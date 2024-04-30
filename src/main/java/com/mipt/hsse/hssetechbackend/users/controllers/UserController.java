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
@RequestMapping("/api/users")
public class UserController {
    HashMap<String, UserConnection> connections;
    //Duration ttl = Duration.of(14, ChronoUnit.DAYS);
    ObjectMapper mapper = new ObjectMapper();
    Random random = new Random();
    JpaUserRepository jpaUserRepository;
    JpaHumanUserPassportRepository jpaHumanUserPassportRepository;

    UserController(JpaUserRepository jpaUserRepository,
                   JpaHumanUserPassportRepository jpaHumanUserPassportRepository) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaHumanUserPassportRepository = jpaHumanUserPassportRepository;
    }

    String AddConnection(HumanUserPassport user) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 14);
        Date end = calendar.getTime();
        random.setSeed(random.nextInt() + user.getYandexId().hashCode());
        UserConnection connection = new UserConnection(user, now, end);
        StringBuilder randomString = new StringBuilder();
        String possibleChars = "!#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        for (int i = 0; i < 50; ++i) {
            randomString.append(possibleChars.charAt(random.nextInt(possibleChars.length())));
        }
        connections.put(randomString.toString(), connection);
        return randomString.toString();
    }

    boolean CheckConnection(String hash) {
        if (!connections.containsKey(hash)) {
            return false;
        }
        UserConnection connection = connections.get(hash);
        Calendar calendar = Calendar.getInstance();
        if (calendar.after(connection.end())) {
            return true;
        }
        connections.remove(hash);
        return false;
    }

    HumanUserPassport GetConnection(String hash) {
        return connections.get(hash).user();
    }

    HumanUserPassport GetUserById(String id) {
        for (UserConnection connection : connections.values()) {
            if (connection.user().getYandexId().equals(id)) {
                return connection.user();
            }
        }
        throw new EntityNotFoundException("User Not Found");
    }

    @PostMapping("register")
    String Register(@RequestBody String body) throws IOException {
        if (!body.contains("psuid")) {
            // todo authorize as lock
            return "";
        }
        YandexToken token = mapper.readValue(body, YandexToken.class);
        int emailId = -1;
        for (int i = 0; i < token.emails().size(); ++i) {
            if (token.emails().get(i).contains("@phystech.edu")) {
                emailId = i;
                break;
            }
        }
        if (emailId == -1)
            throw new IOException("no phystech mail found");
        if (jpaHumanUserPassportRepository.existsByYandexId(token.psuid())) {
            throw new IOException("already exists");
        }
        com.mipt.hsse.hssetechbackend.data.entities.User user =
                new com.mipt.hsse.hssetechbackend.data.entities.User("average");
        if (!jpaUserRepository.exists(Example.of(user))) {
            jpaUserRepository.save(user);
        }
        HumanUserPassport humanUserPassport =
                new HumanUserPassport(token.psuid(), token.display_name(),
                        token.display_name(), token.emails().get(emailId), user);
        jpaHumanUserPassportRepository.save(humanUserPassport);
        humanUserPassport =
                jpaHumanUserPassportRepository.findHumanUserPassportByYandexId(token.psuid());

        return AddConnection(humanUserPassport);
    }

    @GetMapping("{id}")
    User GetUser(@PathVariable Integer id) {
        return null;//TODO
    }

    @PostMapping("{id}/ban")
    void BanUser(@PathVariable UUID id, @RequestBody String cookie) {
        if (CheckConnection(cookie) && GetConnection(cookie).getUser().getRoles().contains(new Role("admin"))) {
            HumanUserPassport passport = GetConnection(cookie);
            passport.setIsBanned(true);
            jpaHumanUserPassportRepository.save(passport);
        }
    }

    @PostMapping("{id}/unban")
    void UnbanUser(@PathVariable UUID id, @RequestBody String cookie) {
        if (CheckConnection(cookie) && GetConnection(cookie).getUser().getRoles().contains(new Role("admin"))) {
            HumanUserPassport passport = GetConnection(cookie);
            passport.setIsBanned(false);
            jpaHumanUserPassportRepository.save(passport);
        }
    }

    @DeleteMapping("{id}")
    void DeleteUser(@PathVariable UUID id, @RequestBody String cookie) {
        if (CheckConnection(cookie) && GetConnection(cookie).getId() == id) {
            HumanUserPassport passport = GetConnection(cookie);
            passport.setIsBanned(true);
            jpaHumanUserPassportRepository.save(passport);
        }
    }

}
