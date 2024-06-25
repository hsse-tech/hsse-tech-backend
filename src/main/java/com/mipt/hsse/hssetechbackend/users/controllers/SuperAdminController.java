package com.mipt.hsse.hssetechbackend.users.controllers;

import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.users.administation.RolesServiceBase;
import com.mipt.hsse.hssetechbackend.users.controllers.requests.ActivateKeyRequest;
import com.mipt.hsse.hssetechbackend.users.controllers.responses.KeyGenResultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/api/sa")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {
  private final RolesServiceBase rolesService;

  public SuperAdminController(RolesServiceBase rolesService) {
    this.rolesService = rolesService;
  }

  @PostMapping("/admins/{id}")
  public void setAdmin(@PathVariable UUID id) {
    rolesService.setUserAdmin(id);
  }

  @DeleteMapping("/admins/{id}")
  public void removeAdmin(@PathVariable UUID id) {
    rolesService.removeAdmin(id);
  }

  @PostMapping("/keygen")
  public ResponseEntity<KeyGenResultResponse> keyGen() {
    return ResponseEntity.ok(
        new KeyGenResultResponse(rolesService.generateSuperAdminActivationKey()));
  }

  @PostMapping("activate-key")
  public void activateKey(@AuthenticationPrincipal OAuth2User user, @RequestBody ActivateKeyRequest activateKeyRequest) {
    rolesService.activateSuperAdmin(OAuth2UserHelper.getUserId(user), activateKeyRequest.key());
  }
}
