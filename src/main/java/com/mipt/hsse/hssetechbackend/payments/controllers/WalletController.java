package com.mipt.hsse.hssetechbackend.payments.controllers;

import com.mipt.hsse.hssetechbackend.payments.controllers.responses.TransactionInfoResponse;
import com.mipt.hsse.hssetechbackend.payments.controllers.responses.WalletInfoResponse;
import com.mipt.hsse.hssetechbackend.payments.services.WalletServiceBase;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class WalletController {
    private final WalletServiceBase walletService;

    public WalletController(WalletServiceBase walletService) {
        this.walletService = walletService;
    }

    //TODO: заменить, когда авторизация будет сделана
    @GetMapping("/api/payments/wallet")
    public WalletInfoResponse getWalletInfo(@RequestParam UUID userId) {
        var wallet = walletService.getWalletByOwner(userId);

        return new WalletInfoResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getTransactions().stream().map(TransactionInfoResponse::from).toList());
    }
}
