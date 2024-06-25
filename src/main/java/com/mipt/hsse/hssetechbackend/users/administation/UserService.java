package com.mipt.hsse.hssetechbackend.users.administation;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserServiceBase {
    private final JpaHumanUserPassportRepository passportRepository;

    public UserService(JpaHumanUserPassportRepository passportRepository) {
        this.passportRepository = passportRepository;
    }

    @Override
    public void banUser(UUID userId) {
        var user = passportRepository.findById(userId).orElseThrow(EntityNotFoundException::new);

        if (isUserAdmin(user)) return;

        user.setIsBanned(true);
    }

    @Override
    public void unbanUser(UUID userId) {
        var user = passportRepository.findById(userId).orElseThrow(EntityNotFoundException::new);

        if (isUserAdmin(user)) return;

        user.setIsBanned(false);
    }

    @Override
    public List<HumanUserPassport> listUsers() {
        return passportRepository.findAll();
    }

    @Override
    public HumanUserPassport getUserById(UUID userId) {
        return passportRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
    }

    private boolean isUserAdmin(HumanUserPassport user) {
        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                return true;
            }
        }

        return false;
    }
}
