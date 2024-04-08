package com.mipt.hsse.hssetechbackend.payments.services.dto;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public record TransactionInfo(BigDecimal amount, UUID walletId, String name, Optional<String> description) {

}
