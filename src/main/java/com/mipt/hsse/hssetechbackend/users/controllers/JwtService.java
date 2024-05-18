package com.mipt.hsse.hssetechbackend.users.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
//    private String jwtSigningKey = "EOaLSZHkZTKe5k9mAD6M8kGiSVKcP7VJRReVhRZ5OFp3zsw4mveP9QKDRGhytaWvFd15rnvdRfhllfa0pv6QkjbitqKgaqneH4cHUCYEDs0pggrXnwg10RpdSSWdtEeHvoKlBXhQQgXSLkRRWR94NQ8z383r52HLKEcyxoYMHtqL2pyL7UKgnF2jCnwdxSL3T5EP1v0cGCR6lVcob47mGrxdGhpBU6uOjhJop2UHgXlI9NLInj2uvznwi58vkEAYdAHxK1NnXxkzBvNLPqmUBz44qIMvw4ZsZ0F2xTSUL82SprKe5yE9sCrmE5Qpa0Fb";

    @Value("${token.signing.key}")//todo
    private String jwtSigningKey;
    private MacAlgorithm custom512 = null;

    public JwtService() {
        //create a custom MacAlgorithm with a custom minKeyBitLength
        try {
            int minKeyBitLength = 30;
            String id = "HS512";
            String jcaName = "HmacSHA512";
            Class<?> c = Class.forName("io.jsonwebtoken.impl.security.DefaultMacAlgorithm");
            Constructor<?> ctor = c.getDeclaredConstructor(String.class, String.class, int.class);
            ctor.setAccessible(true);
            MacAlgorithm custom = (MacAlgorithm) ctor.newInstance(id, jcaName, minKeyBitLength);
            this.custom512 = custom;
        }
        catch (Exception e){

        }
    }

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
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Получение ключа для подписи токена
     *
     * @return ключ
     */
    private SecretKey getSigningKey() {
        var key =
                new SecretKeySpec(jwtSigningKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA512");
        return key;
//        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
//        return Keys.hmacShaKeyFor(keyBytes);
    }
}
