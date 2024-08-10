package com.mipt.hsse.hssetechbackend.controllers.users;

import static org.springframework.http.HttpStatus.OK;

import com.mipt.hsse.hssetechbackend.controllers.users.requests.ActivateKeyRequest;
import com.mipt.hsse.hssetechbackend.controllers.users.responses.KeyGenResultResponse;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.users.administation.RolesServiceBase;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sa")
public class SuperAdminController {
  private final RolesServiceBase rolesService;

  public SuperAdminController(RolesServiceBase rolesService) {
    this.rolesService = rolesService;
  }

  @ResponseStatus(OK)
  @PostMapping("/admins/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> setAdmin(@PathVariable UUID id) {
    rolesService.setUserAdmin(id);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/admins/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> removeAdmin(@PathVariable UUID id) {
    rolesService.removeAdmin(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/keygen")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<KeyGenResultResponse> keyGen() {
    return ResponseEntity.ok(
        new KeyGenResultResponse(rolesService.generateSuperAdminActivationKey()));
  }

  @PostMapping("activate-key")
  @PreAuthorize("hasRole('MIPT_USER')")
  public ResponseEntity<Void> activateKey(@AuthenticationPrincipal OAuth2User user, @RequestBody ActivateKeyRequest activateKeyRequest) {
    rolesService.activateSuperAdmin(OAuth2UserHelper.getUserId(user), activateKeyRequest.key());
    return ResponseEntity.ok().build();
  }
}
