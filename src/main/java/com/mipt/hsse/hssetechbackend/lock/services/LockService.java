package com.mipt.hsse.hssetechbackend.lock.services;

import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemToLockCouplingException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LockService implements LockServiceBase {
  private final JpaItemRepository itemRepository;
  private final JpaLockPassportRepository lockRepository;
  private final JpaHumanUserPassportRepository userRepository;
  private final JpaRentRepository rentRepository;

  public LockService(
      JpaItemRepository itemRepository,
      JpaLockPassportRepository lockRepository,
      JpaHumanUserPassportRepository userRepository,
      JpaRentRepository rentRepository) {
    this.itemRepository = itemRepository;
    this.lockRepository = lockRepository;
    this.userRepository = userRepository;
    this.rentRepository = rentRepository;
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
  @Transactional
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
  @Transactional
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

  /**
   * A user can open a lock if the user is an ADMIN or if at least one item under this lock is now
   * being rented by the user
   */
  @Override
  public boolean canUserOpenLock(UUID userId, UUID lockId) {
    HumanUserPassport user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(HumanUserPassport.class, userId));

    if (user.hasRole("ADMIN")) {
      return true;
    }

    LockPassport lock =
        lockRepository
            .findById(lockId)
            .orElseThrow(() -> new EntityNotFoundException(Lock.class, lockId));

    for (var itemUnderLock : lock.getLockedItems()) {
      Rent currentRentOfItem = rentRepository.getCurrentRentOfItem(itemUnderLock.getId());

      if (currentRentOfItem != null && currentRentOfItem.getRenter().getId().equals(user.getId()))
        return true;
    }

    return false;
  }

  @Override
  public void openLock(UUID lockId) {
    setLockStatus(lockId, true);
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
    setLockStatus(lockId, false);
  }

  private void setLockStatus(UUID lockId, boolean opened) {
    LockPassport lock =
        lockRepository
            .findById(lockId)
            .orElseThrow(() -> new EntityNotFoundException(Lock.class, lockId));
    lock.setOpen(opened);
    lockRepository.save(lock);
  }
}
