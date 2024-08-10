package com.mipt.hsse.hssetechbackend.controllers.payments;

import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.controllers.payments.requests.TopUpBalanceRequest;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.payments.providers.TopUpBalanceProviderBase;
import com.mipt.hsse.hssetechbackend.payments.services.WalletServiceBase;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentsController {
  private final TopUpBalanceProviderBase topUpBalanceProvider;
  private final WalletServiceBase walletService;

  public PaymentsController(TopUpBalanceProviderBase topUpBalanceProvider, WalletServiceBase walletService) {
    this.topUpBalanceProvider = topUpBalanceProvider;
    this.walletService = walletService;
  }

  @PostMapping("top-up-balance")
  public void topUpBalance(
          @AuthenticationPrincipal OAuth2User user,
          @RequestBody TopUpBalanceRequest topUpBalanceRequest,
          HttpServletResponse response) throws IOException {
    var wallet = walletService.getWalletByOwner(OAuth2UserHelper.getUserId(user));

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
