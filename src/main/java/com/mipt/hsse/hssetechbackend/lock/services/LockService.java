package com.mipt.hsse.hssetechbackend.lock.services;

import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.lock.controllers.requests.CreateLockRequest;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemAlreadyHasLockException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import org.springframework.stereotype.Service;

@Service
public class LockService implements LockServiceBase {
  private final JpaItemRepository itemRepository;
  private final JpaLockPassportRepository lockRepository;
  private final JpaHumanUserPassportRepository userRepository;
  private final JpaRentRepository rentRepository;
  private final JpaRoleRepository roleRepository;

  public LockService(
      JpaItemRepository itemRepository,
      JpaLockPassportRepository lockRepository,
      JpaHumanUserPassportRepository userRepository,
      JpaRentRepository rentRepository,
      JpaRoleRepository roleRepository) {
    this.itemRepository = itemRepository;
    this.lockRepository = lockRepository;
    this.userRepository = userRepository;
    this.rentRepository = rentRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public LockPassport createLock(CreateLockRequest request) {
    Item item =
        itemRepository
            .findById(request.itemId())
            .orElseThrow(() -> new EntityNotFoundException(Item.class, request.itemId()));

    LockPassport lock = new LockPassport(item, false);

    return lockRepository.save(lock);
  }

  @Override
  public void deleteLock(UUID id) {
    if (lockRepository.existsById(id)) {
      lockRepository.deleteById(id);
    }
  }

  @Override
  public void updateItemUnderLock(UUID lockId, UUID itemId) throws ItemAlreadyHasLockException {
    if (!itemRepository.existsById(itemId)) throw new EntityNotFoundException(Item.class, itemId);
    if (lockRepository.findByItemId(itemId) != null) throw new ItemAlreadyHasLockException();

    LockPassport lock =
        lockRepository
            .findById(lockId)
            .orElseThrow(() -> new EntityNotFoundException(Lock.class, lockId));
    Item item =
        itemRepository
            .findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException(Item.class, itemId));
    lock.setItem(item);

    lockRepository.save(lock);
  }

  @Override
  public boolean canUserOpenLock(UUID userId, UUID lockId) {
    HumanUserPassport user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(HumanUserPassport.class, userId));

    // Admin can open any locks
    Role adminRole = roleRepository.findByName("ADMIN");
    if (user.hasRole(adminRole)) {
      return true;
    }

    LockPassport lock =
        lockRepository
            .findById(lockId)
            .orElseThrow(() -> new EntityNotFoundException(Lock.class, lockId));
    UUID itemUnderLockId = lock.getItem().getId();
    Item itemUnderLock = itemRepository.findById(itemUnderLockId).orElseThrow();
    Rent currentRentOfItem = rentRepository.getCurrentRentOfItem(itemUnderLock.getId());

    // A user can open a lock if the item under the lock is rented by them
    return currentRentOfItem != null && currentRentOfItem.getRenter().getId().equals(user.getId());
  }

  @Override
  public void openLock(UUID lockId) {
    setLockOpened(lockId, true);
  }

  @Override
  public boolean isLockOpen(UUID lockId) {
    LockPassport lock =
        lockRepository
            .findById(lockId)
            .orElseThrow(() -> new EntityNotFoundException(Lock.class, lockId));
    return lock.isOpen();
  }

  @Override
  public void closeLock(UUID lockId) {
    setLockOpened(lockId, false);
  }

  private void setLockOpened(UUID lockId, boolean opened) {
    LockPassport lock =
        lockRepository
            .findById(lockId)
            .orElseThrow(() -> new EntityNotFoundException(Lock.class, lockId));
    lock.setOpen(opened);
    lockRepository.save(lock);
  }
}
