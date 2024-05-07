package com.mipt.hsse.hssetechbackend.payments.controllers;

import com.mipt.hsse.hssetechbackend.payments.controllers.requests.TopUpBalanceRequest;
import com.mipt.hsse.hssetechbackend.payments.providers.TopUpBalanceProviderBase;
import com.mipt.hsse.hssetechbackend.payments.services.WalletServiceBase;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;

@Controller
@RequestMapping("/api/payment")
public class PaymentsController {
  private final TopUpBalanceProviderBase topUpBalanceProvider;
  private final WalletServiceBase walletService;

  public PaymentsController(TopUpBalanceProviderBase topUpBalanceProvider, WalletServiceBase walletService) {

    this.topUpBalanceProvider = topUpBalanceProvider;
    this.walletService = walletService;
  }

  @PostMapping("top-up-balance")
  public RedirectView topUpBalance(@RequestBody TopUpBalanceRequest topUpBalanceRequest) {
    var wallet = walletService.getWalletByOwner(topUpBalanceRequest.userId());

    var result = topUpBalanceProvider.topUpBalance(wallet.getId(), BigDecimal.valueOf(topUpBalanceRequest.amount()));

    return new RedirectView(result.paymentUrl());
  }
}
