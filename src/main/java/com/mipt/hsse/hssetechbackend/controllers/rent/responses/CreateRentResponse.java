package com.mipt.hsse.hssetechbackend.controllers.rent.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import lombok.Getter;

@Getter
public class CreateRentResponse {
  @JsonProperty("rent")
  private final Rent rent;

  @JsonProperty("error-message")
  private final String errorMessage;

  @JsonCreator
  private CreateRentResponse(
      @JsonProperty("rent") Rent rent, @JsonProperty("error-message") String errorMessage) {
    this.rent = rent;
    this.errorMessage = errorMessage;
  }

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
