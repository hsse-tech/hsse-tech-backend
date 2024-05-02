package com.mipt.hsse.hssetechbackend.users.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    //    @Value("${token.signing.key}")//todo
    private String jwtSigningKey = "EOaLSZHkZTKe5k9mAD6M8kGiSVKcP7VJRReVhRZ5OFp3zsw4mveP9QKDRGhytaWvFd15rnvdRfhllfa0pv6QkjbitqKgaqneH4cHUCYEDs0pggrXnwg10RpdSSWdtEeHvoKlBXhQQgXSLkRRWR94NQ8z383r52HLKEcyxoYMHtqL2pyL7UKgnF2jCnwdxSL3T5EP1v0cGCR6lVcob47mGrxdGhpBU6uOjhJop2UHgXlI9NLInj2uvznwi58vkEAYdAHxK1NnXxkzBvNLPqmUBz44qIMvw4ZsZ0F2xTSUL82SprKe5yE9sCrmE5Qpa0Fb";

    /**
     * Извлечение имени пользователя из токена
     *
     * @param token токен
     * @return имя пользователя
     */
    public String extractUserName(String token) {
        return extractAllClaims(token).get("uid").toString();
    }

    /**
     * Генерация токена
     *
     * @param userDetails данные пользователя
     * @return токен
     */
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        if (userDetails instanceof User customUserDetails) {
//            claims.put("id", customUserDetails.getId());
////            claims.put("email", customUserDetails.getEmail());
//            claims.put("role", customUserDetails.getRoles());
//        }
//        return generateToken(claims, userDetails);
//    }

    /**
     * Проверка токена на валидность
     *
     * @param token       токен
     * @param userDetails данные пользователя
     * @return true, если токен валиден
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (userDetails == null || token == null)
            return false;
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Извлечение данных из токена
     *
     * @param token           токен
     * @param claimsResolvers функция извлечения данных
     * @param <T>             тип данных
     * @return данные
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Генерация токена
     *
     * @param extraClaims дополнительные данные
     * @param userDetails данные пользователя
     * @return токен
     */
//    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 100000 * 60 * 24))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
//    }

    /**
     * регистрация пользователя
     */
    public HumanUserPassport isUserOk(String token) {
        var claims = extractAllClaims(token);
        return new HumanUserPassport(claims.get("uid").toString(),
                claims.get(
                        "login", String.class), claims.get("psuid",
                String.class),
                claims.get("email", String.class), null);
    }

    /**
     * Проверка токена на просроченность
     *
     * @param token токен
     * @return true, если токен просрочен
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлечение даты истечения токена
     *
     * @param token токен
     * @return дата истечения
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлечение всех данных из токена
     *
     * @param token токен
     * @return данные
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
                .getBody();
    }

    /**
     * Получение ключа для подписи токена
     *
     * @return ключ
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
