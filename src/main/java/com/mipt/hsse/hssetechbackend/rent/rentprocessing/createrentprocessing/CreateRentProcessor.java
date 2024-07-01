package com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing;

import com.mipt.hsse.hssetechbackend.utils.VerificationResult;

/**
 * Provides interface to perform any required actions before a rent is created, i.e. added to the
 * database. <br>
 * The processors are launched as a single transaction. Therefore, any exception thrown in
 * processCreate() will result in the rollback of the whole transaction and will mean the failure of
 * the creation of the rent.
 */
@FunctionalInterface
public interface CreateRentProcessor {
  VerificationResult processCreate(CreateRentProcessData createRentData);
}
