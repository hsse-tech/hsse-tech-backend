package com.mipt.hsse.hssetechbackend.rent.rentprocessing.createRentProcessing;

import com.mipt.hsse.hssetechbackend.data.repositories.JpaRentRepository;
import com.mipt.hsse.hssetechbackend.rent.customexceptions.RentProcessingException;
import org.springframework.stereotype.Component;

/**
 * Verifies that the time bounds of a given rent do not intersect with the time bounds of any
 * existing rents
 */
@Component
public class UnoccupiedTimeCreateRentProcessor implements CreateRentProcessor {
  private final JpaRentRepository rentRepository;

  public UnoccupiedTimeCreateRentProcessor(JpaRentRepository rentRepository) {
    this.rentRepository = rentRepository;
  }

  /** Verify that each existing rent either starts before or finishes after the given rent */
  @Override
  public void processCreate(CreateRentProcessData createRentData) {
    var item = createRentData.rent().getItem();
    var from = createRentData.rent().getStartAt();
    var to = createRentData.rent().getEndedAt();
    int countIntersecting = rentRepository.countRentsIntersectingTimeBounds(item, from, to);

    if (countIntersecting > 0)
      throw new RentProcessingException(
          "Unoccupied time check failed:\nTime from "
              + from
              + " to "
              + to
              + " is occupied already");
  }
}
