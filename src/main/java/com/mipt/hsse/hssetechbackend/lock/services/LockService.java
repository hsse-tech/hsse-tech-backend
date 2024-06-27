package com.mipt.hsse.hssetechbackend.lock.services;

import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import com.mipt.hsse.hssetechbackend.lock.controllers.requests.CreateLockRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LockService implements LockServiceBase {

  @Override
  public LockPassport createLock(CreateLockRequest request) {
    return null;
  }

  @Override
  public void deleteLock(UUID id) {

  }

  @Override
  public void updateItemUnderLock(UUID lockId, UUID uuid) {

  }

  @Override
  public boolean canUserOpenLock(UUID userId, UUID lockId) {
    return false;
  }

  @Override
  public void openLock(UUID lockId) {

  }

  @Override
  public boolean isLockOpen(UUID lockId) {
    return false;
  }

  @Override
  public void closeLock(UUID lockId) {

  }
}
