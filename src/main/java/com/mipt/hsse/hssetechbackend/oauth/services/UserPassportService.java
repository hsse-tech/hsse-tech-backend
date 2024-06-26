package com.mipt.hsse.hssetechbackend.oauth.services;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPassportService implements UserPassportServiceBase {
    private static final String FIRST_NAME_ATTR = "first_name";
    private static final String LAST_NAME_ATTR = "last_name";
    private static final String EMAIL_ATTR = "default_email";
    private static final String YANDEX_ID_ATTR = "id";
    
    private final JpaHumanUserPassportRepository passportRepository;

    public UserPassportService(JpaHumanUserPassportRepository passportRepository) {
        this.passportRepository = passportRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public HumanUserPassport findOrCreateByYandexId(OAuth2User user) {
        var targetPassport = passportRepository.findByYandexId(Long.parseLong(user.getName()));

        if (targetPassport == null) {
            targetPassport = passportRepository.save(generatePassport(user));
        }

        return targetPassport;
    }

    private HumanUserPassport generatePassport(OAuth2User user) {
        var attributes = user.getAttributes();
        return new HumanUserPassport(
            Long.parseLong(attributes.get(YANDEX_ID_ATTR).toString()),
            attributes.get(FIRST_NAME_ATTR).toString(),
            attributes.get(LAST_NAME_ATTR).toString(),
            attributes.get(EMAIL_ATTR).toString()
        );
    }
}
