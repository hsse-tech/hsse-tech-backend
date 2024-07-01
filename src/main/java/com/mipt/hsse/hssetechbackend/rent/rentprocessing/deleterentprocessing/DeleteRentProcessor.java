package com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing;

import com.mipt.hsse.hssetechbackend.utils.VerificationResult;
import com.mipt.hsse.hssetechbackend.rent.exceptions.RentProcessingException;

/**
 * Provides interface to perform any required actions before a rent gets deleted from the rent
 * database.<br>
 * The processors are launched as a single transaction. Therefore, any exception thrown in
 * processCreate() will result in the rollback of the whole transaction and will mean the failure of
 * the removal of the rent.
 */
@FunctionalInterface
public interface DeleteRentProcessor {
  VerificationResult processDelete(DeleteRentProcessData deleteRentData) throws RentProcessingException;
}
