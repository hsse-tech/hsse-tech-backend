package com.mipt.hsse.hssetechbackend.apierrorhandling;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
  public static ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
    return new ResponseEntity<>(apiError, apiError.getStatus());
  }

  @ExceptionHandler(value = EntityNotFoundException.class)
  public ResponseEntity<ApiError> buildResponseEntity(HttpServletRequest request, EntityNotFoundException entityNotFoundException) {
    return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, entityNotFoundException.getMessage()));
  }
}
