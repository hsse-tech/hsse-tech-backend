package com.mipt.hsse.hssetechbackend.payments.controllers;

import com.mipt.hsse.hssetechbackend.payments.controllers.requests.TinkoffEventRequest;
import com.mipt.hsse.hssetechbackend.payments.providers.events.AcquiringEventsListener;
import com.mipt.hsse.hssetechbackend.payments.providers.events.MerchantNotification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/api/payment")
public class TinkoffEventsListenerController {
  private final AcquiringEventsListener eventsListener;

  public TinkoffEventsListenerController(AcquiringEventsListener eventsListener) {
    this.eventsListener = eventsListener;
  }

  @PostMapping("status")
  public ResponseEntity<String> status(@RequestBody TinkoffEventRequest request) {
    eventsListener.onAcquiringNotificationReceived(new MerchantNotification(request.amount(), request.orderId(), request.success(), request.status()));
    return ResponseEntity.of(Optional.of("OK"));
  }

  @ExceptionHandler
  public ResponseEntity<String> exceptionHandler(Exception e) {
    return ResponseEntity.of(Optional.of("OK"));
  }
}
