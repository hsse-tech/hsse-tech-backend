package com.mipt.hsse.hssetechbackend.payments.controllers.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletInfoResponse(UUID id, BigDecimal balance, Iterable<TransactionInfoResponse> transactions) {}
