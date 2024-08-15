package com.mipt.hsse.hssetechbackend.controllers.users.responses;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GetHumanUserResponse(
    @NotNull UUID id,
    @NotNull @NotEmpty String firstName,
    @NotNull @NotEmpty String lastName,
    @NotNull @NotEmpty @Email String email) {

  public GetHumanUserResponse(HumanUserPassport userPassport) {
    this(
        userPassport.getId(),
        userPassport.getFirstName(),
        userPassport.getLastName(),
        userPassport.getEmail());
  }
}
