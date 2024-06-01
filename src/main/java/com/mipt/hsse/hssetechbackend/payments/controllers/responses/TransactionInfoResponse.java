package com.mipt.hsse.hssetechbackend.payments.controllers.responses;

import com.mipt.hsse.hssetechbackend.data.entities.ClientTransactionStatus;
import com.mipt.hsse.hssetechbackend.data.entities.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionInfoResponse(String name, BigDecimal amount, String description, Instant createdAt, ClientTransactionStatus status) {
    public static TransactionInfoResponse from(Transaction transaction) {
        return new TransactionInfoResponse(transaction.getName(), transaction.getAmount(), transaction.getDescription(), transaction.getCreatedAt(), transaction.getStatus());
    }
}
