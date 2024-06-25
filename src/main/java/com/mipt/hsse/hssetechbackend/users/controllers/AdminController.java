package com.mipt.hsse.hssetechbackend.users.controllers;

import com.mipt.hsse.hssetechbackend.users.administation.UserServiceBase;
import com.mipt.hsse.hssetechbackend.users.controllers.responses.GetHumanUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
  private final UserServiceBase userService;

  public AdminController(UserServiceBase userService) {
    this.userService = userService;
  }

  @PostMapping("ban/{id}")
  public void ban(@PathVariable UUID id) {
    userService.banUser(id);
  }

  @PostMapping("unban/{id}")
  public void unban(@PathVariable UUID id) {
    userService.unbanUser(id);
  }

  @GetMapping("users")
  public ResponseEntity<List<GetHumanUserResponse>> listAllUsers() {
      return ResponseEntity.ok(
              userService.listUsers().stream().map(GetHumanUserResponse::new).toList());
  }

  @GetMapping("users/{id}")
  public ResponseEntity<GetHumanUserResponse> getUserById(@PathVariable UUID id) {
    return ResponseEntity.ok(new GetHumanUserResponse(userService.getUserById(id)));
  }
}
