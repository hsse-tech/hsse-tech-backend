package com.mipt.hsse.hssetechbackend.users.administation;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;

import java.util.List;
import java.util.UUID;

public interface UserServiceBase {
    void banUser(UUID userId);
    void unbanUser(UUID userId);
    List<HumanUserPassport> listUsers();
    HumanUserPassport getUserById(UUID userId);
}
