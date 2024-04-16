package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateRentResponse {
  private final Rent rent;
  private final String errorMessage;

  public static CreateRentResponse respondSuccess(Rent rent) {
    return new CreateRentResponse(rent, "");
  }

  public static CreateRentResponse respondFailed(String errorMessage) {
    if (errorMessage.isEmpty()) {
      throw new IllegalArgumentException("Error message must not be empty");
    }
    return new CreateRentResponse(null, errorMessage);
  }
}
