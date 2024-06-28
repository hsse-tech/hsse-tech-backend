package com.mipt.hsse.hssetechbackend.lock.controllers;

import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import com.mipt.hsse.hssetechbackend.lock.controllers.requests.CreateLockRequest;
import com.mipt.hsse.hssetechbackend.lock.controllers.requests.UpdateLockRequest;
import com.mipt.hsse.hssetechbackend.lock.controllers.responses.CreateLockResponse;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemAlreadyHasLockException;
import com.mipt.hsse.hssetechbackend.lock.services.LockServiceBase;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/locks")
public class LockController {
  private final LockServiceBase lockService;

  public LockController(LockServiceBase lockService) {
    this.lockService = lockService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CreateLockResponse> createLock(@Valid @RequestBody CreateLockRequest request) {
    LockPassport lock = lockService.createLock(request);
    CreateLockResponse response = new CreateLockResponse(lock.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @DeleteMapping("{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteLock(@PathVariable("id") UUID id) {
    lockService.deleteLock(id);
  }

  @PatchMapping("{lock_id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> updateItemUnderLock(@PathVariable("lock_id") UUID lockId, @Valid @RequestBody UpdateLockRequest request) {
    try {
      lockService.updateItemUnderLock(lockId, request.itemId());
      return ResponseEntity.noContent().build();
    } catch (ItemAlreadyHasLockException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("{id}/open")
  public ResponseEntity<Void> openLock(@PathVariable("id") UUID lockId, @AuthenticationPrincipal OAuth2User principal) {
    var userId = OAuth2UserHelper.getUserId(principal);
    if (lockService.canUserOpenLock(userId, lockId)){
      lockService.openLock(lockId);
      return ResponseEntity.noContent().build();
    } else {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping("{id}/is-open")
  public ResponseEntity<Boolean> isLockOpen(@PathVariable("id") UUID lockId) {
    boolean isOpen = lockService.isLockOpen(lockId);
    if (isOpen) {
      lockService.closeLock(lockId);
    }
    return ResponseEntity.ok(isOpen);
  }


  @ExceptionHandler({EntityNotFoundException.class})
  public ResponseEntity<ApiError> exceptionHandler(
      Exception ex) {
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    return RestExceptionHandler.buildResponseEntity(apiError);
  }
}
