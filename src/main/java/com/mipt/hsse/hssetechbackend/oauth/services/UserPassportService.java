package com.mipt.hsse.hssetechbackend.oauth.services;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import org.springframework.stereotype.Service;

@Service
public class UserPassportService implements UserPassportServiceBase {
    private final JpaHumanUserPassportRepository passportRepository;

    public UserPassportService(JpaHumanUserPassportRepository passportRepository) {
        this.passportRepository = passportRepository;
    }

    @Override
    public HumanUserPassport findOrCreateByYandexId(Long yandexId) {
        return passportRepository.findByYandexId(yandexId);
    }
}
