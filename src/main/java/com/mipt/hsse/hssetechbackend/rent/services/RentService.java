package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.controllers.rent.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.controllers.rent.requests.UpdateRentRequest;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRentRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepository.PhotoType;
import com.mipt.hsse.hssetechbackend.rent.exceptions.*;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessor;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing.DeleteRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing.DeleteRentProcessor;
import com.mipt.hsse.hssetechbackend.utils.VerificationResult;
import jakarta.transaction.Transactional;
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
  private final PhotoRepository photoRepository;
  private final long MIN_RENT_DURATION_MINUTES;

  public RentService(
      JpaRentRepository rentRepository,
      JpaHumanUserPassportRepository userRepository,
      JpaItemRepository itemRepository,
      List<CreateRentProcessor> createRentProcessors,
      List<DeleteRentProcessor> deleteRentProcessors,
      PhotoRepository photoRepository,
      @Value("${min-rent-duration-minutes}") long minRentDurationMinutes) {
    this.rentRepository = rentRepository;
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
    this.createRentProcessors = createRentProcessors;
    this.deleteRentProcessors = deleteRentProcessors;
    this.photoRepository = photoRepository;
    MIN_RENT_DURATION_MINUTES = minRentDurationMinutes;
  }

  @Transactional
  public Rent createRent(UUID userId, CreateRentRequest request) {
    Item item =
        itemRepository
            .findById(request.itemId())
            .orElseThrow(() -> new EntityNotFoundException(Item.class, request.itemId()));

    HumanUserPassport renter =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(HumanUserPassport.class, userId));

    Rent rent = new Rent(request.name(), request.description(), request.startTime(), request.endTime(), renter, item);

    verifyRentStartEnd(request.startTime(), request.endTime(), rent).throwIfInvalid();

    // Rent processing
    CreateRentProcessData processData = new CreateRentProcessData(rent);
    for (var processor : createRentProcessors) {
      VerificationResult verificationResult = processor.processCreate(processData);
      if (!verificationResult.isValid()) {
        throw new CreateRentProcessingException(verificationResult.getErrorMessage());
      }
    }

    return rentRepository.save(rent);
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
        throw new DeleteRentProcessingException(verificationResult.getErrorMessage());
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
    Rent rent = rentOpt.orElseThrow(() -> EntityNotFoundException.rentNotFound(rentId));

    verifyRentStart(rent).throwIfInvalid();

    rent.setFactStart(Instant.now());

    rentRepository.save(rent);
  }

  public void endRent(UUID rentId) {
    Optional<Rent> rentOpt = rentRepository.findById(rentId);
    Rent rent = rentOpt.orElseThrow(() -> EntityNotFoundException.rentNotFound(rentId));

    verifyRentEnd(rent).throwIfInvalid();

    rent.setFactEnd(Instant.now());

    rentRepository.save(rent);
  }

  public byte[] getPhotoForRent(UUID rentId) {
    if (rentRepository.existsById(rentId)) {
      try {
        return photoRepository.findPhoto(PhotoType.RENT_CONFIRMATION, rentId);
      } catch (IOException e) {
        throw new ServerErrorException("Unexpected IO error while saving photo", e);
      }
    } else {
      throw new EntityNotFoundException(Rent.class, rentId);
    }
  }

  public void confirmRentFinish(UUID rentId, byte[] photoBytes) {
    Optional<Rent> rentOpt = rentRepository.findById(rentId);
    Rent rent = rentOpt.orElseThrow(() -> EntityNotFoundException.rentNotFound(rentId));

    verifyConfirmRentFinish(rent).throwIfInvalid();

    try {
      photoRepository.save(PhotoType.RENT_CONFIRMATION, rentId, photoBytes);
    } catch (IOException | NoSuchAlgorithmException | UnsupportedOperationException e) {
      throw new ServerErrorException("Unexpected IO error while saving photo", e);
    }
  }

  @Transactional
  public void updateRent(UUID rentId, UpdateRentRequest request) {
    Rent rent = rentRepository.findById(rentId).orElseThrow(() -> EntityNotFoundException.rentNotFound(rentId));

    if (rent.getPlannedStart().isBefore(Instant.now())) {
      throw new VerificationFailedException(
          "This rent cannot be updated now because it has already started");
    }
    if (request.newEndTime().isBefore(Instant.now())) {
      throw new VerificationFailedException("New end time cannot be after current time");
    }

    // Check that the new time bounds of rent will not intersect with any other rents
    // Note that they may intersect with this rent itself
    List<Rent> getIntersectingRents =
        rentRepository.getIntersectingRentsOfItem(
            rent.getItem(), request.newStartTime(), request.newEndTime());
    if (getIntersectingRents.size() > 1
        || getIntersectingRents.size() == 1
            && getIntersectingRents.getFirst().getId() != rent.getId()) {
      throw new VerificationFailedException(
          "The new time bounds intersect with already existing rent(-s) of the same item");
    }

    // Verify time bounds
    verifyRentStartEnd(request.newStartTime(), request.newEndTime(), rent).throwIfInvalid();

    rent.setPlannedStart(request.newStartTime());
    rent.setPlannedEnd(request.newEndTime());

    rentRepository.save(rent);
  }

  // region Time-related rent verification
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
        && !photoRepository.existsPhoto(PhotoType.RENT_CONFIRMATION, rent.getId())) {
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
    } else if (photoRepository.existsPhoto(PhotoType.RENT_CONFIRMATION, rent.getRenter().getId())) {
      error = "A photo confirmation for this rent has already been uploaded";
    }

    if (!error.equals(NO_ERROR)) return VerificationResult.buildInvalid(error);
    else return VerificationResult.buildValid();
  }
  // endregion
}
