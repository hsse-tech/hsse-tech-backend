package com.mipt.hsse.hssetechbackend.users.controllers.requests;

import java.util.List;

public record YandexToken
        (String display_name,
         String login,
         int id,
         String client_id,
         String psuid,
         List<String> emails) {
}
