package com.mipt.hsse.hssetechbackend.lock.services;

import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemToLockCouplingException;
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
  public LockPassport createLock() {
    LockPassport lock = new LockPassport();
    return lockRepository.save(lock);
  }

  @Override
  public void deleteLock(UUID id) {
    if (lockRepository.existsById(id)) {
      lockRepository.deleteById(id);
    }
  }

  @Override
  public void addItemToLock(UUID lockId, UUID itemId) throws ItemToLockCouplingException {
    LockPassport lock =
        lockRepository
            .findById(lockId)
            .orElseThrow(() -> new EntityNotFoundException(Lock.class, lockId));
    Item item =
        itemRepository
            .findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException(Item.class, itemId));

    if (item.getLock() != null)
      throw new ItemToLockCouplingException(
          "Cannot add lock to this item because it already has a lock");

    lock.addItem(item);

    lockRepository.save(lock);
    itemRepository.save(item);
  }

  @Override
  public void removeItemFromLock(UUID lockId, UUID itemId) throws ItemToLockCouplingException {
    LockPassport lock =
        lockRepository
            .findById(lockId)
            .orElseThrow(() -> new EntityNotFoundException(Lock.class, lockId));
    Item item =
        itemRepository
            .findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException(Item.class, itemId));

    if (!lock.doesLockItem(item))
      throw new ItemToLockCouplingException(
          "Cannot remove lock from this item because this item is not locked by this lock");

    lock.removeItem(item);

    lockRepository.save(lock);
    itemRepository.save(item);
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

    // A user can open a lock if there is at least 1 iem under this lock, which is now rented by
    // this user
    for (var itemUnderLock : lock.getLockedItems()) {
      Rent currentRentOfItem = rentRepository.getCurrentRentOfItem(itemUnderLock.getId());

      if (currentRentOfItem != null && currentRentOfItem.getRenter().getId().equals(user.getId()))
        return true;
    }

    return false;
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
