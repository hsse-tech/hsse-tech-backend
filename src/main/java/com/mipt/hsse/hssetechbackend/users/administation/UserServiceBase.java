package com.mipt.hsse.hssetechbackend.users.administation;

import java.util.UUID;

public interface UserServiceBase {
    void banUser(UUID userId);
    void unbanUser(UUID userId);
}
