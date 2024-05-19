package com.mipt.hsse.hssetechbackend.apierrorhandling;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiError {
  private HttpStatus status;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private LocalDateTime timestamp;
  private String message;

  private ApiError() {
    timestamp = LocalDateTime.now();
  }

  public ApiError(HttpStatus status) {
    this();
    this.status = status;
  }

  public ApiError(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
