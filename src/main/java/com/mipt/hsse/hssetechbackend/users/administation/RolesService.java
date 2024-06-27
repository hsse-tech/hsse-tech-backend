package com.mipt.hsse.hssetechbackend.users.administation;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRoleRepository;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.newSetFromMap;

@Service
public class RolesService implements RolesServiceBase {
  private final JpaRoleRepository roleRepository;
  private final JpaHumanUserPassportRepository passportRepository;

  private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";
  private static final String MIPT_USER_ROLE_NAME = "ROLE_MIPT_USER";
  private static final String SUPER_ADMIN_ROLE_NAME = "ROLE_SUPER_ADMIN";

  private static final Set<String> activationKeys = newSetFromMap(new ConcurrentHashMap<>());

  public RolesService(
      JpaRoleRepository roleRepository, JpaHumanUserPassportRepository passportRepository) {
    this.roleRepository = roleRepository;
    this.passportRepository = passportRepository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void setupRoles() {
    for (String roleName : List.of(ADMIN_ROLE_NAME, MIPT_USER_ROLE_NAME, SUPER_ADMIN_ROLE_NAME)) {
      setupRole(roleName);
    }
  }

  private void setupRole(String roleName) {
    if (!roleRepository.existsByName(roleName)) {
      roleRepository.save(new Role(roleName));
    }
  }

  @Override
  public Optional<HumanUserPassport> getSuperAdmin() {
    return roleRepository.findByName(SUPER_ADMIN_ROLE_NAME).getUsers().stream().findFirst();
  }

  @Override
  public String generateSuperAdminActivationKey() {
    var key = UUID.randomUUID().toString();

    activationKeys.add(key);

    return key;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void activateSuperAdmin(UUID userId, String activationKey) {
    var targetPassport =
        passportRepository.findById(userId).orElseThrow(EntityNotFoundException::new);

    if (!activationKeys.contains(activationKey)) {
      throw new EntityNotFoundException("Activation Key not found");
    }

    var targetRole = roleRepository.findByName(SUPER_ADMIN_ROLE_NAME);
    var adminRole = roleRepository.findByName(ADMIN_ROLE_NAME);
    removeOldSuperAdminIfExists(targetRole);
    activationKeys.remove(activationKey);

    targetRole.addUser(targetPassport);

    if (!targetPassport.getRoles().contains(adminRole)) {
      adminRole.addUser(targetPassport);
    }

    roleRepository.save(targetRole);
    roleRepository.save(adminRole);
  }

  private void removeOldSuperAdminIfExists(Role superAdminRole) {
    var superAdmin = getSuperAdmin();

    if (superAdmin.isEmpty()) return;
    superAdminRole.removeUser(superAdmin.get());
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void setUserAdmin(UUID userId) {
    var adminRole = roleRepository.findByName(ADMIN_ROLE_NAME);
    var superAdminRole = roleRepository.findByName(SUPER_ADMIN_ROLE_NAME);
    var targetUser =
        passportRepository
            .findById(userId)
            .orElseThrow(
                () -> EntityNotFoundException.userNotFound(userId));

    if (targetUser.getRoles().contains(superAdminRole)) {
      return;
    }

    adminRole.addUser(targetUser);
    roleRepository.save(adminRole);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void removeAdmin(UUID userId) {
    var adminRole = roleRepository.findByName(ADMIN_ROLE_NAME);
    var superAdminRole = roleRepository.findByName(SUPER_ADMIN_ROLE_NAME);
    var targetUser =
        passportRepository
            .findById(userId)
            .orElseThrow(
                () -> EntityNotFoundException.userNotFound(userId));

    if (targetUser.getRoles().contains(superAdminRole)) {
      return;
    }

    adminRole.removeUser(targetUser);
    roleRepository.save(adminRole);
  }
}
