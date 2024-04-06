package com.mipt.hsse.hssetechbackend.payments.services.dto;

import java.util.Optional;
import java.util.UUID;

public record TransactionInfo(double amount, UUID walletId, String name, Optional<String> description) {

}
