package com.mipt.hsse.hssetechbackend.lock.services;

import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemToLockCouplingException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LockServiceBase {

  LockPassport createLock();

  void deleteLock(UUID id);

  boolean canUserOpenLock(UUID userId, UUID lockId);

  void openLock(UUID lockId);

  void addItemToLock(UUID lockId, UUID itemId) throws ItemToLockCouplingException;

  void removeItemFromLock(UUID lockId, UUID itemId) throws ItemToLockCouplingException;

  Optional<LockPassport> findById(UUID id);

  List<LockPassport> findAll();
}
