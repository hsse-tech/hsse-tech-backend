package com.mipt.hsse.hssetechbackend.users.administation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

public class RoleEnvSetuper {
    private final RolesServiceBase rolesService;
    private final Logger logger = LoggerFactory.getLogger(RoleEnvSetuper.class);

    public RoleEnvSetuper(RolesServiceBase rolesService) {
        this.rolesService = rolesService;
    }

    @Scheduled
    @Transactional
    public void setup() {
        rolesService.setupRoles();

        if (rolesService.getSuperAdmin().isEmpty()) {
            logger.warn("Super administrator not found. Activation key: {}", rolesService.generateSuperAdminActivationKey());
        }
    }
}
