package com.mipt.hsse.hssetechbackend.payments.rent;

import com.mipt.hsse.hssetechbackend.data.entities.ClientTransactionStatus;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionServiceBase;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import com.mipt.hsse.hssetechbackend.rent.exceptions.RentProcessingException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessData;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PaymentCreateRentProcessor implements CreateRentProcessor {
  private final TransactionServiceBase transactionService;
  private final float HOUR_SECONDS = 60 * 60;

  public PaymentCreateRentProcessor(TransactionServiceBase transactionService) {
    this.transactionService = transactionService;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void processCreate(CreateRentProcessData createRentData) throws RentProcessingException {
    var rent = createRentData.rent();
    var targetItem = rent.getItem();
    var targetItemType = targetItem.getType();
    var costByHour = targetItemType.getCost();
    var rentTimeHour = (rent.getEndedAt().getEpochSecond() - rent.getStartAt().getEpochSecond()) / HOUR_SECONDS;
    var cost = costByHour.multiply(BigDecimal.valueOf(rentTimeHour));

    var renter = rent.getRenter();
    var wallet = renter.getWallet();

    var transName = "Аренда \"%s\"".formatted(targetItem.getDisplayName());

    var trans = transactionService.createTransaction(new TransactionInfo(cost, wallet.getId(), transName, Optional.empty()));
    transactionService.setTransactionStatus(trans.getId(), ClientTransactionStatus.SUCCESS);
  }
}
