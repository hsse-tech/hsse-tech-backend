package com.mipt.hsse.hssetechbackend.payments.rent;

import com.mipt.hsse.hssetechbackend.auxiliary.VerificationResult;
import com.mipt.hsse.hssetechbackend.data.entities.ClientTransactionStatus;
import com.mipt.hsse.hssetechbackend.payments.RentCostCalculator;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletUpdatingException;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionServiceBase;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PaymentCreateRentProcessor implements CreateRentProcessor {
  private final TransactionServiceBase transactionService;


  public PaymentCreateRentProcessor(TransactionServiceBase transactionService) {
    this.transactionService = transactionService;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public VerificationResult processCreate(CreateRentProcessData createRentData) {
    var rent = createRentData.rent();
    var targetItem = rent.getItem();
    var rentCost = RentCostCalculator.calculate(
            targetItem, createRentData.rent().getStartAt(), createRentData.rent().getEndedAt());
    var renter = rent.getRenter();
    var wallet = renter.getWallet();

    var transName = "Аренда \"%s\"".formatted(targetItem.getDisplayName());

    try {
      var trans = transactionService.createTransaction(new TransactionInfo(rentCost, wallet.getId(), transName, Optional.empty()));
      transactionService.setTransactionStatus(trans.getId(), ClientTransactionStatus.SUCCESS);

      return VerificationResult.buildValid();
    } catch (WalletUpdatingException e) {
      return VerificationResult.buildInvalid("Not enough money");
    }
  }
}
