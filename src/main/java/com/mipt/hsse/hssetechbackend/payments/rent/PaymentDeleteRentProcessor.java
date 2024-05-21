package com.mipt.hsse.hssetechbackend.payments.rent;

import com.mipt.hsse.hssetechbackend.auxiliary.VerificationResult;
import com.mipt.hsse.hssetechbackend.payments.RentCostCalculator;
import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionManipulationException;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionService;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import com.mipt.hsse.hssetechbackend.rent.exceptions.RentProcessingException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing.DeleteRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing.DeleteRentProcessor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentDeleteRentProcessor implements DeleteRentProcessor {
  private final TransactionService transactionService;

  public PaymentDeleteRentProcessor(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @Override
  public VerificationResult processDelete(DeleteRentProcessData deleteRentData) throws RentProcessingException {
    var rent = deleteRentData.rent();
    var targetItem = rent.getItem();
    var rentCost = RentCostCalculator.calculate(
            targetItem, deleteRentData.rent().getPlannedStart(), deleteRentData.rent().getPlannedEnd())
            .negate();
    var renter = rent.getRenter();
    var wallet = renter.getWallet();

    var transName = "Возврат средств за аренду вещи \"%s\"".formatted(targetItem.getDisplayName());

    var transInfo = new TransactionInfo(rentCost, wallet.getId(), transName, Optional.empty());
    var targetTrans = transactionService.createTransaction(transInfo);

    try {
      transactionService.commitTransaction(targetTrans.getId());
    } catch (TransactionManipulationException e) {
      return VerificationResult.buildInvalid("Failed to commit transaction");
    }

    return VerificationResult.buildValid();
  }
}
