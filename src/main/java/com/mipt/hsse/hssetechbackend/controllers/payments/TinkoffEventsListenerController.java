package com.mipt.hsse.hssetechbackend.controllers.payments;

import com.mipt.hsse.hssetechbackend.controllers.payments.requests.TinkoffEventRequest;
import com.mipt.hsse.hssetechbackend.payments.providers.events.AcquiringEventsListener;
import com.mipt.hsse.hssetechbackend.payments.providers.events.MerchantNotification;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
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
