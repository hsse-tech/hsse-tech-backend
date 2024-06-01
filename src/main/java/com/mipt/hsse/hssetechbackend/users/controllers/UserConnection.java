package com.mipt.hsse.hssetechbackend.users.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;

import java.util.Date;

public record UserConnection(HumanUserPassport user,
                             Date start,
                             Date end) {
}
