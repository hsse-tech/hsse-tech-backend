package com.mipt.hsse.hssetechbackend.payments.controllers;

import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.payments.controllers.requests.TopUpBalanceRequest;
import com.mipt.hsse.hssetechbackend.payments.providers.TopUpBalanceProviderBase;
import com.mipt.hsse.hssetechbackend.payments.services.WalletServiceBase;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequestMapping("/api/payment")
@PreAuthorize("false")
public class PaymentsController {
  private final TopUpBalanceProviderBase topUpBalanceProvider;
  private final WalletServiceBase walletService;

  public PaymentsController(TopUpBalanceProviderBase topUpBalanceProvider, WalletServiceBase walletService) {
    this.topUpBalanceProvider = topUpBalanceProvider;
    this.walletService = walletService;
  }

  @PostMapping("top-up-balance")
  @PreAuthorize("false")
  public void topUpBalance(@RequestBody TopUpBalanceRequest topUpBalanceRequest, HttpServletResponse response) throws IOException {
    var wallet = walletService.getWalletByOwner(topUpBalanceRequest.userId());

    var result = topUpBalanceProvider.topUpBalance(wallet.getId(), BigDecimal.valueOf(topUpBalanceRequest.amount()));

    if (result.successfullyCreated()) {
      response.sendRedirect(result.paymentUrl());
    } else {
      throw new RuntimeException("Top up balance returned failed");
    }
  }

  @ExceptionHandler
  public ResponseEntity<ApiError> exceptionHandler(Exception e) {
    return RestExceptionHandler.buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, e.getMessage()));
  }
}
