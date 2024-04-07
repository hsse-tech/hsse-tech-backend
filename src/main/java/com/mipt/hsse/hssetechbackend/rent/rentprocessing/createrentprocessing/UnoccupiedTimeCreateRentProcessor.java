package com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing;

import com.mipt.hsse.hssetechbackend.auxiliary.VerificationResult;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRentRepository;
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
  public VerificationResult processCreate(CreateRentProcessData createRentData) {
    var item = createRentData.rent().getItem();
    var from = createRentData.rent().getPlannedStart();
    var to = createRentData.rent().getPlannedEnd();
    boolean isDisjointWithOtherRents =
        rentRepository.isDisjointWithOtherRentsOfSameItem(item, from, to);

    if (!isDisjointWithOtherRents)
      return VerificationResult.buildInvalid(
          "Unoccupied time check failed:\nTime from "
              + from
              + " to "
              + to
              + " is occupied already");
    else return VerificationResult.buildValid();
  }
}
