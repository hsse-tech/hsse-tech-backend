package com.mipt.hsse.hssetechbackend.users.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.users.controllers.requests.YandexToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.mipt.hsse.hssetechbackend.data.entities.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;
import java.net.http.*;
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


    @PostMapping("/register")
    String Register(@RequestBody String body) {
        return "you have been registred";
    }

    @PostMapping("/auth")
    String Auth(@RequestBody String body) {
        return "you have been authkfjdked";
    }

    @GetMapping("/yandex_oauth_callback")
    public ResponseEntity<?> yandexOauth(@RequestParam String code,
                                         HttpServletResponse response) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.yandex.ru/info?format=jwt")).header("Authorization","OAuth "+code).build();
        HttpResponse yaResp = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        code = yaResp.body().toString();
        Cookie cookie = new Cookie("Authorization", code);
        //создаем объект Cookie,
        //в конструкторе указываем значения для name и value
        cookie.setPath("/");//устанавливаем путь
//        cookie.setMaxAge(86400);//здесь устанавливается время жизни куки
        response.addCookie(cookie);//добавляем Cookie в запрос
        response.setContentType("text/plain");
        response.sendRedirect("/");
        return ResponseEntity.ok().body(HttpStatus.MOVED_PERMANENTLY);
    }
    @GetMapping("/adminonly")
    String admo(){
        return "u are admin";
    }
    @GetMapping("/useronly")
    String usro(){
        return "u are user";
    }
    @GetMapping("/free")
    String free(){
        return "u are free";
    }
    @GetMapping("/anyone")
    String anyone(){
        return "u are anyone";
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
