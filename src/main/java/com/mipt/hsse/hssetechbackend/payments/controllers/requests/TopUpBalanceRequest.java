package com.mipt.hsse.hssetechbackend.payments.controllers.requests;

import java.util.UUID;

// TODO: До авторизации временная мера получения ID пользователя через сущность
public record TopUpBalanceRequest(UUID userId, double amount) {
}
