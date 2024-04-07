package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.auxiliary.VerificationResult;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.ConfirmationPhotoRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRentRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.PinPhotoConfirmationRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.RentProcessingException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessor;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing.DeleteRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing.DeleteRentProcessor;
import jakarta.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;

@Service
public class RentService {
  private final JpaRentRepository rentRepository;
  private final JpaHumanUserPassportRepository userRepository;
  private final JpaItemRepository itemRepository;
  private final List<CreateRentProcessor> createRentProcessors;
  private final List<DeleteRentProcessor> deleteRentProcessors;
  private final ConfirmationPhotoRepository photoRepository;
  private final long MIN_RENT_DURATION_MINUTES;
  
  public RentService(
      JpaRentRepository rentRepository,
      JpaHumanUserPassportRepository userRepository,
      JpaItemRepository itemRepository,
      List<CreateRentProcessor> createRentProcessors,
      List<DeleteRentProcessor> deleteRentProcessors,
      ConfirmationPhotoRepository photoRepository,
      @Value("${max-rent-duration-minutes}") long minRentDurationMinutes) {
    this.rentRepository = rentRepository;
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
    this.createRentProcessors = createRentProcessors;
    this.deleteRentProcessors = deleteRentProcessors;
    this.photoRepository = photoRepository;
    MIN_RENT_DURATION_MINUTES = minRentDurationMinutes;
  }

  @Transactional
  public Rent createRent(CreateRentRequest request) {
    Item item =
        itemRepository
            .findById(request.itemId())
            .orElseThrow(() -> new EntityNotFoundException(Item.class, request.itemId()));

    HumanUserPassport renter =
        userRepository
            .findById(request.userId())
            .orElseThrow(() -> new EntityNotFoundException(User.class, request.userId()));

    Rent rent = new Rent(request.startTime(), request.endTime(), renter, item);

    verifyRentStartEnd(request.startTime(), request.endTime(), rent).throwIfInvalid();

    // Rent processing
    CreateRentProcessData processData = new CreateRentProcessData(rent);
    for (var processor : createRentProcessors) {
      VerificationResult verificationResult = processor.processCreate(processData);
      if (!verificationResult.isValid()) {
        throw new RentProcessingException(verificationResult.getErrorMessage());
      }
    }

    rentRepository.save(rent);

    return rent;
  }

  @Transactional
  public void deleteRent(UUID rentId) {
    Rent rent =
        rentRepository
            .findById(rentId)
            .orElseThrow(() -> new EntityNotFoundException(Rent.class, rentId));

    DeleteRentProcessData processData = new DeleteRentProcessData(rent);

    for (var processor : deleteRentProcessors) {
      VerificationResult verificationResult = processor.processDelete(processData);
      if (!verificationResult.isValid()) {
        throw new RentProcessingException(verificationResult.getErrorMessage());
      }
    }

    rentRepository.delete(rent);
  }

  public Rent findById(UUID rentId) {
    return rentRepository
        .findById(rentId)
        .orElseThrow(() -> new EntityNotFoundException(Rent.class, rentId));
  }

  public void startRent(UUID rentId) {
    Optional<Rent> rentOpt = rentRepository.findById(rentId);
    Rent rent = rentOpt.orElseThrow(EntityNotFoundException::new);

    verifyRentStart(rent).throwIfInvalid();

    rent.setFactStart(Instant.now());

    rentRepository.save(rent);
  }

  public void endRent(UUID rentId) {
    Optional<Rent> rentOpt = rentRepository.findById(rentId);
    Rent rent = rentOpt.orElseThrow(EntityNotFoundException::new);

    verifyRentEnd(rent).throwIfInvalid();

    rent.setFactEnd(Instant.now());

    rentRepository.save(rent);
  }

  public ByteArrayInputStream getPhotoForRent(UUID rentId) {
    if (rentRepository.existsById(rentId)) {
      try {
        byte[] photoBytes = photoRepository.getPhotoForRent(rentId);
        return new ByteArrayInputStream(photoBytes);
      } catch (IOException e) {
        throw new ServerErrorException("Unexpected IO error while saving photo", e);
      }
    } else {
      throw new EntityNotFoundException(Rent.class, rentId);
    }
  }

  public void confirmRentFinish(UUID rentId, PinPhotoConfirmationRequest request) {
    Optional<Rent> rentOpt = rentRepository.findById(rentId);
    Rent rent = rentOpt.orElseThrow(EntityNotFoundException::new);

    verifyConfirmRentFinish(rent).throwIfInvalid();

    try {
      photoRepository.save(rentId, request.photoBytes());
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new ServerErrorException("Unexpected IO error while saving photo", e);
    }

  }

  private VerificationResult verifyRentStartEnd(Instant start, Instant end, Rent rent) {
    Duration rentDuration = Duration.between(start, end);
    long rentDurationMinutes = rentDuration.toMinutes();
    long maxItemDurationMinutes = rent.getItem().getType().getMaxRentTimeMinutes();

    String error = "";
    if (rentDuration.isNegative()) {
      error = "Planned start time cannot be later than planned end time";
    }
    if (rentDurationMinutes < MIN_RENT_DURATION_MINUTES) {
      error = "The duration of rent cannot be less than the minimum rent duration";
    }
    if (rentDurationMinutes > maxItemDurationMinutes) {
      error =
          "The duration of rent cannot be larger that the maximum duration for this item, which is: "
              + maxItemDurationMinutes
              + " min";
    }

    if (!error.isEmpty()) return VerificationResult.buildInvalid(error);
    else return VerificationResult.buildValid();
  }

  private VerificationResult verifyRentStart(Rent rent) {
    final String NO_ERROR = "NO_ERROR";
    String error = NO_ERROR;
    if (rent.getFactEnd() != null) {
      error = "This rent has already been finished";
    } else if (rent.getFactStart() != null) {
      error = "This rent has already been started; it is in process";
    } else if (Instant.now().isBefore(rent.getPlannedStart())) {
      error = "This rent cannot be started because its planned start time has not come yet";
    } else if (Instant.now().isAfter(rent.getPlannedEnd())) {
      error = "This rent cannot be started because its planned end time has already come";
    }

    if (!error.equals(NO_ERROR)) return VerificationResult.buildInvalid(error);
    else return VerificationResult.buildValid();
  }

  private VerificationResult verifyRentEnd(Rent rent) {
    final String NO_ERROR = "NO_ERROR";
    String error = NO_ERROR;
    if (rent.getFactEnd() != null) {
      error = "This rent has already been finished";
    } else if (rent.getFactStart() == null) {
      error = "This rent has not yet been started";
    }

    if (rent.getItem().getType().isPhotoRequiredOnFinish()
        && !photoRepository.existsPhotoForRent(rent.getId())) {
      error =
          "The rent cannot be finished, because it required a photo confirmation that has not been receiver";
    }

    if (!error.equals(NO_ERROR)) return VerificationResult.buildInvalid(error);
    else return VerificationResult.buildValid();
  }

  private VerificationResult verifyConfirmRentFinish(Rent rent) {
    final String NO_ERROR = "NO_ERROR";
    String error = NO_ERROR;
    if (rent.getFactStart() == null) {
      error = "This rent has not started yet";
    } else if (rent.getFactEnd() != null) {
      error = "This rent has finished already";
    } else if (!rent.getItem().getType().isPhotoRequiredOnFinish()) {
      error = "This rent does not require photo confirmation";
    } else if (photoRepository.existsPhotoForRent(rent.getRenter().getId())) {
      error = "A photo confirmation for this rent has already been uploaded";
    }

    if (!error.equals(NO_ERROR)) return VerificationResult.buildInvalid(error);
    else return VerificationResult.buildValid();
  }
}
