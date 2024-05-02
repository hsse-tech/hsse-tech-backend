package com.mipt.hsse.hssetechbackend.users.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.users.controllers.requests.YandexToken;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.mipt.hsse.hssetechbackend.data.entities.User;

import java.io.IOException;
import java.util.*;

@Controller
//@RequestMapping("/api/users")
public class UserController {
    JpaUserRepository jpaUserRepository;
    JpaHumanUserPassportRepository jpaHumanUserPassportRepository;

    UserController(JpaUserRepository jpaUserRepository,
                   JpaHumanUserPassportRepository jpaHumanUserPassportRepository) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaHumanUserPassportRepository = jpaHumanUserPassportRepository;
    }


    @PostMapping("register")
    String Register(@RequestBody String body) {
        return "you have been registred";
    }

    @PostMapping("auth")
    String Auth(@RequestBody String body) {
        return "you have been authkfjdked";
    }

    @GetMapping("/api/users/{idS}")
    HumanUserPassport GetUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);
        return jpaHumanUserPassportRepository.findHumanUserPassportById(id);
    }

    @PostMapping("api/admin/{idS}/ban")
    void BanUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);
        HumanUserPassport passport =
                jpaHumanUserPassportRepository.findHumanUserPassportById(id);
        passport.setIsBanned(true);
        jpaHumanUserPassportRepository.save(passport);
    }


    @PostMapping("api/admin/{idS}/unban")
    void UnbanUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);

        HumanUserPassport passport =
                jpaHumanUserPassportRepository.findHumanUserPassportById(id);
        passport.setIsBanned(false);
        jpaHumanUserPassportRepository.save(passport);

    }

    @DeleteMapping("api/admin/{idS}")
    void DeleteUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);
        HumanUserPassport passport =
                jpaHumanUserPassportRepository.findHumanUserPassportById(id);
        passport.setIsBanned(true);
        jpaHumanUserPassportRepository.save(passport);
    }

}
