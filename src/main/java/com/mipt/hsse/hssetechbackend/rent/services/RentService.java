package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRentRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.RentItemRequest;
import com.mipt.hsse.hssetechbackend.rent.customexceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createRentProcessing.CreateRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createRentProcessing.CreateRentProcessor;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleteRentProcessing.DeleteRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleteRentProcessing.DeleteRentProcessor;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class RentService {
  private final JpaRentRepository rentRepository;
  private final JpaUserRepository userRepository;
  private final JpaItemRepository itemRepository;
  private final List<CreateRentProcessor> createRentProcessors;
  private final List<DeleteRentProcessor> deleteRentProcessors;

  public RentService(
      JpaRentRepository rentRepository,
      JpaUserRepository userRepository,
      JpaItemRepository itemRepository,
      List<CreateRentProcessor> createRentProcessors,
      List<DeleteRentProcessor> deleteRentProcessors) {
    this.rentRepository = rentRepository;
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
    this.createRentProcessors = createRentProcessors;
    this.deleteRentProcessors = deleteRentProcessors;
  }

  @Transactional
  public Rent rentItem(RentItemRequest request) {
    Item item =
        itemRepository
            .findById(request.itemId())
            .orElseThrow(() -> new EntityNotFoundException(Item.class, request.itemId()));

    User renter =
        userRepository
            .findById(request.userId())
            .orElseThrow(() -> new EntityNotFoundException(User.class, request.userId()));

    Rent rent = new Rent(request.startTime(), request.endTime(), renter, item);

    CreateRentProcessData processData =
        new CreateRentProcessData(rent);

    try {
      for (var processor : createRentProcessors) {
        processor.processCreate(processData);
      }
    } catch (NumberFormatException e) {
      throw new RestClientException("Failed to create a new rent: processing stage failed");
    }

    rentRepository.save(rent);

    return rent;
  }

  @Transactional
  public void unrentItem(UUID rentId) {
    Rent rent =
        rentRepository
            .findById(rentId)
            .orElseThrow(() -> new EntityNotFoundException(Rent.class, rentId));

    DeleteRentProcessData processData = new DeleteRentProcessData(rent);

    for (var processor : deleteRentProcessors) {
     processor.processDelete(processData);
    }

    rentRepository.delete(rent);
  }

  public Rent findById(UUID rentId) {
    return rentRepository
        .findById(rentId)
        .orElseThrow(() -> new EntityNotFoundException(Rent.class, rentId));
  }
}
