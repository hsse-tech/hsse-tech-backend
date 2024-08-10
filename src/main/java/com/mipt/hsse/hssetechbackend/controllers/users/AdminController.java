package com.mipt.hsse.hssetechbackend.controllers.users;

import com.mipt.hsse.hssetechbackend.controllers.users.responses.GetHumanUserResponse;
import com.mipt.hsse.hssetechbackend.users.administation.UserServiceBase;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
  private final UserServiceBase userService;

  public AdminController(UserServiceBase userService) {
    this.userService = userService;
  }

  @PostMapping("ban/{id}")
  public ResponseEntity<Void> ban(@PathVariable UUID id) {
    userService.banUser(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("unban/{id}")
  public ResponseEntity<Void> unban(@PathVariable UUID id) {
    userService.unbanUser(id);
    return ResponseEntity.ok().build();
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
