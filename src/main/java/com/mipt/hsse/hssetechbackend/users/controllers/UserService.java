package com.mipt.hsse.hssetechbackend.users.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRoleRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JpaUserRepository repository;
    private final JpaRoleRepository roles;

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        return repository.save(user);
    }


    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            // Заменить на свои исключения
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

//        if (repository.existsByEmail(user.getEmail())) {
//            throw new RuntimeException("Пользователь с таким email уже существует");
//        }

        return save(user);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        return (User) repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

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
    public User getCurrentUser() {
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
        var userRoles = user.getRoles();
        if (!roles.existsByName("admin")) {
            var adminRole = new Role("admin");
            roles.save(adminRole);
        }
        userRoles.add(roles.getRoleByName("admin"));
        user.setRoles(userRoles);
        save(user);
    }
}