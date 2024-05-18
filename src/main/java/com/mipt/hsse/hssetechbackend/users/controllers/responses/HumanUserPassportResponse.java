package com.mipt.hsse.hssetechbackend.users.controllers.responses;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class HumanUserPassportResponse {

    public UUID id;

    public String yandexId;

    public String firstName;

    public String lastName;

    public Boolean isBanned = false;

    public String email;

    public List<Rent> rents;

    public Wallet wallet;

    public HumanUserPassportResponse(HumanUserPassport passport) {
        this.id = passport.getId();
        this.yandexId = passport.getYandexId();
        this.firstName = passport.getFirstName();
        this.lastName = passport.getLastName();
        this.isBanned = passport.getIsBanned();
        this.email = passport.getEmail();
        this.rents = passport.getRents();
        this.wallet = passport.getWallet();
    }
}
