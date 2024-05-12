package com.mipt.hsse.hssetechbackend.users.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRoleRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JpaHumanUserPassportRepository repository;
    private final JpaRoleRepository roles;
    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public HumanUserPassport save(HumanUserPassport user) {
        return repository.save(user);
    }


    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public HumanUserPassport create(HumanUserPassport user) {
        if (repository.existsByYandexId(user.getUsername())) {
            // Заменить на свои исключения
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

//        if (repository.existsByEmail(user.getEmail())) {
//            throw new RuntimeException("Пользователь с таким email уже существует");
//        }
        if (user.getUser() == null) {
            user.setUser(new User("user"));
            var userRoles = user.getUser().getRoles();
            if (!roles.existsByName("ROLE_USER")) {
                var adminRole = new Role("ROLE_USER");
                roles.save(adminRole);
            }
            userRoles.add(roles.getRoleByName("ROLE_USER"));
        }
        return save(user);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public HumanUserPassport getByUsername(String username) {
        return repository.findHumanUserPassportByYandexId(username)
                .orElse(null);

    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public HumanUserPassport getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }


    /**
     * Выдача прав администратора текущему пользователю
     * <p>
     * Нужен для демонстрации
     */
    @Deprecated
    public void getAdmin() {
        var user = getCurrentUser();
        var userRoles = user.getUser().getRoles();
        if (!roles.existsByName("ROLE_ADMIN")) {
            var adminRole = new Role("ROLE_ADMIN");
            roles.save(adminRole);
        }
        userRoles.add(roles.getRoleByName("ROLE_ADMIN"));
        user.getUser().setRoles(userRoles);
        save(user);
    }
}