package com.mipt.hsse.hssetechbackend.users.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.users.controllers.requests.YandexToken;
import com.mipt.hsse.hssetechbackend.users.controllers.responses.HumanUserPassportResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.mipt.hsse.hssetechbackend.data.entities.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;
import java.net.http.*;

@RestController
//@RequestMapping("/api/users")
public class UserController {
    JpaUserRepository jpaUserRepository;
    JpaHumanUserPassportRepository jpaHumanUserPassportRepository;

    UserController(JpaUserRepository jpaUserRepository,
                   JpaHumanUserPassportRepository jpaHumanUserPassportRepository) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaHumanUserPassportRepository = jpaHumanUserPassportRepository;
    }


    @PostMapping("/register")
    String Register(@RequestBody String body) {
        return "you have been registred";
    }

    @PostMapping("/auth")
    String Auth(@RequestBody String body) {
        return "you have been authkfjdked";
    }

    @GetMapping("/yandex_oauth_callback")
    public String redirectYandex() {
        return """ 
                <html><script>window.location.href = window.location.href.split('#')[0]+"Second?"+window.location.href.split('#')[1];</script></html>
                """;
    }

    @GetMapping("/yandex_oauth_callbackSecond")
    public ResponseEntity<?> yandexOauth(@RequestParam String access_token,
                                         HttpServletResponse response) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.yandex.ru/info?format=jwt")).header("Authorization", "OAuth " + access_token).build();
        HttpResponse yaResp = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        access_token = yaResp.body().toString();
        Cookie cookie = new Cookie("Authorization", access_token);
        //создаем объект Cookie,
        //в конструкторе указываем значения для name и value
        cookie.setPath("/");//устанавливаем путь
//        cookie.setMaxAge(86400);//здесь устанавливается время жизни куки
        response.addCookie(cookie);//добавляем Cookie в запрос
        response.setContentType("text/plain");
        response.sendRedirect("/");
        return ResponseEntity.ok().body(HttpStatus.MOVED_PERMANENTLY);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/adminonly")
    String admo() {
        return "u are admin";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/useronly")
    String usro() {
        return "u are user";
    }

    @GetMapping("/free")
    @ResponseStatus(HttpStatus.OK)
    public String free() {
        return "resp";
    }

    @GetMapping("/anyone")
    @ResponseStatus(HttpStatus.OK)
    String anyone() {
        return "u are anyone";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users/{idS}")
    HumanUserPassportResponse GetUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);
        if (!jpaHumanUserPassportRepository.existsById(id)) {
            return null;
        }
        return new HumanUserPassportResponse(jpaHumanUserPassportRepository.findHumanUserPassportById(id));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/users/profile")
    HumanUserPassportResponse GetSelf() {
        var id =
                ((HumanUserPassport) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getYandexId();
        if (!jpaHumanUserPassportRepository.existsByYandexId(id)) {
            return null;
        }
        return new HumanUserPassportResponse(jpaHumanUserPassportRepository.findHumanUserPassportByYandexId(id).get());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/{idS}/ban")
    void BanUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);
        if (!jpaHumanUserPassportRepository.existsById(id)) {
            return;
        }
        HumanUserPassport passport =
                jpaHumanUserPassportRepository.findHumanUserPassportById(id);
        passport.setIsBanned(true);
        jpaHumanUserPassportRepository.save(passport);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/{idS}/unban")
    void UnbanUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);
        if (!jpaHumanUserPassportRepository.existsById(id)) {
            return;
        }

        HumanUserPassport passport =
                jpaHumanUserPassportRepository.findHumanUserPassportById(id);
        passport.setIsBanned(false);
        jpaHumanUserPassportRepository.save(passport);

    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/{idS}")
    void DeleteUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);
        if (!jpaHumanUserPassportRepository.existsById(id)) {
            return;
        }
        HumanUserPassport passport =
                jpaHumanUserPassportRepository.findHumanUserPassportById(id);
        passport.setIsBanned(true);
        jpaHumanUserPassportRepository.save(passport);
    }

}
