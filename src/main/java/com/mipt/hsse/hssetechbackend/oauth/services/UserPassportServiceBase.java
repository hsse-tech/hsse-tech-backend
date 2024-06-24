package com.mipt.hsse.hssetechbackend.oauth.services;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;

public interface UserPassportServiceBase {
    HumanUserPassport findByYandexId(Long yandexId);
}
