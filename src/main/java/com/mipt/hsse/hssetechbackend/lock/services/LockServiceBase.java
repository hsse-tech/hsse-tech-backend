package com.mipt.hsse.hssetechbackend.lock.services;

import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import com.mipt.hsse.hssetechbackend.lock.controllers.requests.CreateLockRequest;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemAlreadyHasLockException;

import java.util.UUID;

public interface LockServiceBase {

  LockPassport createLock(CreateLockRequest request);

  void deleteLock(UUID id);

  void updateItemUnderLock(UUID lockId, UUID uuid) throws ItemAlreadyHasLockException;

  boolean canUserOpenLock(UUID userId, UUID lockId);

  void openLock(UUID lockId);

  boolean isLockOpen(UUID lockId);

  void closeLock(UUID lockId);
}
