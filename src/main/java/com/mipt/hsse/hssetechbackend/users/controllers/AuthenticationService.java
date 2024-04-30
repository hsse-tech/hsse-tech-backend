package com.mipt.hsse.hssetechbackend.users.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRoleRepository;
import com.mipt.hsse.hssetechbackend.users.controllers.requests.SignUpRequest;
import com.mipt.hsse.hssetechbackend.users.controllers.responses.JwtAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.mipt.hsse.hssetechbackend.users.controllers.requests.SignInRequest;
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final JpaRoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        var user = new User("human");
        var userRoles = user.getRoles();
        if (!roles.existsByName("user")) {
            var adminRole = new Role("user");
            roles.save(adminRole);
        }
        userRoles.add(roles.getRoleByName("user"));
        user.setRoles(userRoles);
        //todo set password
        //todo set email

        userService.create(user);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}