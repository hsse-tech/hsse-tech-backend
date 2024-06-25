package com.mipt.hsse.hssetechbackend.users.administation;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRoleRepository;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(RolesService.class)
class RolesServiceTest extends DatabaseSuite {
    @Autowired
    private JpaRoleRepository roleRepository;

    @Autowired
    private JpaHumanUserPassportRepository passportRepository;

    @Autowired
    private RolesService rolesService;

    private HumanUserPassport testPassport;

    @BeforeEach
    public void setupRoles() {
        rolesService.setupRoles();
        testPassport = passportRepository.save(new HumanUserPassport(123L, "Денис", "Войтенко", "voitenko.da@phystech.edu"));
    }

    @AfterEach
    public void clearRoles() {
        passportRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    public void testActivateSuperAdminNoSuperAdminBefore() {
        var activationKey = rolesService.generateSuperAdminActivationKey();

        assertEquals(0, roleRepository.findByName("ROLE_SUPER_ADMIN").getUsers().size());
        rolesService.activateSuperAdmin(testPassport.getId(), activationKey);

        var targetUser = roleRepository.findByName("ROLE_SUPER_ADMIN").getUsers().stream().findFirst();

        assertTrue(targetUser.isPresent());
        assertEquals(testPassport.getId(), targetUser.get().getId());
    }

    @Test
    public void testActivateSuperAdminAlreadySuperAdminBefore() {
        var activationKey = rolesService.generateSuperAdminActivationKey();
        var oldAdmin = passportRepository.save(new HumanUserPassport(1234L, "Иванов", "Иванович", "ivanov.ia@phystech.edu"));

        passportRepository.save(oldAdmin);

        rolesService.activateSuperAdmin(oldAdmin.getId(), activationKey);

        assertEquals(1, roleRepository.findByName("ROLE_SUPER_ADMIN").getUsers().size());

        activationKey = rolesService.generateSuperAdminActivationKey();

        rolesService.activateSuperAdmin(testPassport.getId(), activationKey);

        var superAdmins = roleRepository.findByName("ROLE_SUPER_ADMIN").getUsers();

        assertEquals(1, superAdmins.size());

        var targetUser = superAdmins.stream().findFirst();

        assertTrue(targetUser.isPresent());
        assertEquals(testPassport.getId(), targetUser.get().getId());
    }

    @Test
    public void testActivateSuperAdminKeyAlreadyActivated() {
        var activationKey = rolesService.generateSuperAdminActivationKey();
        var oldAdmin = passportRepository.save(new HumanUserPassport(1234L, "Иванов", "Иванович", "ivanov.ia@phystech.edu"));

        passportRepository.save(oldAdmin);

        rolesService.activateSuperAdmin(oldAdmin.getId(), activationKey);
        assertThrows(EntityNotFoundException.class, () -> rolesService.activateSuperAdmin(testPassport.getId(), activationKey));

        var users = roleRepository.findByName("ROLE_SUPER_ADMIN").getUsers();

        assertEquals(1, users.size());
        assertEquals(oldAdmin.getId(), users.stream().findFirst().orElseThrow().getId());
    }
}
