package com.mipt.hsse.hssetechbackend.payments.rent;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionServiceBase;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = PaymentCreateRentProcessor.class)
public class CreatingRentTests {
  @MockBean
  private TransactionServiceBase transactionService;

  @Autowired
  private PaymentCreateRentProcessor paymentRentProc;

  @Test
  public void testCreateShouldPass() {
    when(transactionService.createTransaction(any())).thenReturn(new Transaction(BigDecimal.valueOf(100.00), "Test", "Test"));

    paymentRentProc.processCreate(new CreateRentProcessData(new Rent(Instant.now(), Instant.now(), null, null)));
    verify(transactionService.createTransaction(any()));
  }
}
