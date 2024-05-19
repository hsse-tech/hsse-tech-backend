package com.mipt.hsse.hssetechbackend.users.controllers;


import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.users.controllers.responses.HumanUserPassportResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpClient;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
                <html><head>
                    <title>redirect</title>
                                
                    <meta charset="utf-8">
                    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">\s
                </head>
                                
                <body class="vsc-initialized">
                <script>
                    window.location.href = window.location.href.split('#')[0]+"Second?"+window.location.href.split('#')[1];
                </script>
                </body></html>
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

    @GetMapping("/adminonly")
    String admo() {
        return "u are admin";
    }

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

    @GetMapping("/api/users/{idS}")
    HumanUserPassportResponse GetUser(@PathVariable String idS) {
        var id = UUID.fromString(idS);
        if (!jpaHumanUserPassportRepository.existsById(id)) {
            return null;
        }
        return new HumanUserPassportResponse(jpaHumanUserPassportRepository.findHumanUserPassportById(id));
    }

    @GetMapping("/api/users/profile")
    HumanUserPassportResponse GetSelf() {
        var id =
                ((HumanUserPassport) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        if (!jpaHumanUserPassportRepository.existsByYandexId(id)) {
            return null;
        }
        return new HumanUserPassportResponse(jpaHumanUserPassportRepository.findHumanUserPassportByYandexId(id).get());
    }

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
