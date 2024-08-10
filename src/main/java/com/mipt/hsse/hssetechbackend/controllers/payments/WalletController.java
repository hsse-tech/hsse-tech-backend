package com.mipt.hsse.hssetechbackend.controllers.payments;

import com.mipt.hsse.hssetechbackend.controllers.payments.responses.TransactionInfoResponse;
import com.mipt.hsse.hssetechbackend.controllers.payments.responses.WalletInfoResponse;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.payments.services.WalletServiceBase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {
    private final WalletServiceBase walletService;

    public WalletController(WalletServiceBase walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/api/payments/wallet")
    public WalletInfoResponse getWalletInfo(@AuthenticationPrincipal OAuth2User principal) {
        var wallet = walletService.getWalletByOwner(OAuth2UserHelper.getUserId(principal));

        return new WalletInfoResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getTransactions().stream().map(TransactionInfoResponse::from).toList());
    }
}
