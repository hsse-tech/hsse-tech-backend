package com.mipt.hsse.hssetechbackend.controllers.lock;

import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.controllers.lock.responses.CreateLockResponse;
import com.mipt.hsse.hssetechbackend.controllers.lock.responses.GetLockResponse;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemToLockCouplingException;
import com.mipt.hsse.hssetechbackend.lock.services.LockServiceBase;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locks")
public class LockController {
  private final LockServiceBase lockService;

  public LockController(LockServiceBase lockService) {
    this.lockService = lockService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CreateLockResponse> createLock() {
    LockPassport lock = lockService.createLock();
    CreateLockResponse response = new CreateLockResponse(lock.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GetLockResponse> getLock(@PathVariable("id") UUID id) {
    Optional<LockPassport> lockPassport = lockService.findById(id);
    if (lockPassport.isPresent()) {
      List<UUID> lockedItemsIds =
          lockPassport.get().getLockedItems().stream().map(Item::getId).toList();
      GetLockResponse response = new GetLockResponse(lockPassport.get().getId(), lockedItemsIds);
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<GetLockResponse>> getAllLocks() {
    List<LockPassport> locks = lockService.findAll();
    List<GetLockResponse> responses = locks.stream().map(GetLockResponse::fromLock).toList();
    return ResponseEntity.ok(responses);
  }

  @DeleteMapping("{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteLock(@PathVariable("id") UUID id) {
    lockService.deleteLock(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("{lock_id}/add_item/{item_id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> addItemToLock(
      @PathVariable("lock_id") UUID lockId, @PathVariable("item_id") UUID itemId) {
    try {
      lockService.addItemToLock(lockId, itemId);
      return ResponseEntity.noContent().build();
    } catch (ItemToLockCouplingException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PatchMapping("{lock_id}/remove_item/{item_id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> removeItemFromLock(
      @PathVariable("lock_id") UUID lockId, @PathVariable("item_id") UUID itemId) {
    try {
      lockService.removeItemFromLock(lockId, itemId);
      return ResponseEntity.noContent().build();
    } catch (ItemToLockCouplingException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("{id}/open")
  public ResponseEntity<Void> openLock(
      @PathVariable("id") UUID lockId, @AuthenticationPrincipal OAuth2User principal) {
    var userId = OAuth2UserHelper.getUserId(principal);
    if (lockService.canUserOpenLock(userId, lockId)) {
      lockService.openLock(lockId);
      return ResponseEntity.noContent().build();
    } else {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }

  @ExceptionHandler({EntityNotFoundException.class})
  public ResponseEntity<ApiError> exceptionHandler(Exception ex) {
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    return RestExceptionHandler.buildResponseEntity(apiError);
  }
}
