package com.mipt.hsse.hssetechbackend.users.administation;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;

import java.util.Optional;
import java.util.UUID;

public interface RolesServiceBase {
    /**
     * Подготавливает все для роли
     */
    void setupRoles();

    /**
     * Получает супер-админа, если он существует
     */
    Optional<HumanUserPassport> getSuperAdmin();

    /**
     * Генерирует ключ трансфера админ роли
     */
    String generateSuperAdminActivationKey();

    /**
     * Активирует трансфер супер админа
     */
    void activateSuperAdmin(UUID userId, String superAdminActivationKey);

    /**
     * Назначает пользователя на роль администратора
     */
    void setUserAdmin(UUID userId);

    /**
     * Убирает роль админа
     */
    void removeAdmin(UUID userId);
}
