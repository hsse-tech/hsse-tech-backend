package com.mipt.hsse.hssetechbackend.payments.controllers;

import com.mipt.hsse.hssetechbackend.payments.controllers.requests.TopUpBalanceRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/payment")
public class PaymentsController {
  @PostMapping("top-up-balance")
  public void topUpBalance(@RequestBody TopUpBalanceRequest topUpBalanceRequest) {

  }
}
