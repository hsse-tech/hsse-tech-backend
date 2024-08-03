package com.mipt.hsse.hssetechbackend.oauth.services;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.users.administation.RolesService;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(UserPassportService.class)
class UserPassportServiceTest extends DatabaseSuite {
  @Autowired private JpaHumanUserPassportRepository passportRepository;

  @Autowired private UserPassportService passportService;

  @MockBean private RolesService rolesService;

  @AfterEach
  public void clear() {
    passportRepository.deleteAll();
  }

  @Test
  public void testFindAlreadyExists() {
    passportRepository.save(
        new HumanUserPassport(123L, "Денис", "Войтенко", "voitenko.da@phystech.edu"));

    var targetPassport =
        passportService.findOrCreateByYandexId(
            createUser(123L, "Денис", "Войтенко", "voitenko.da@phystech.edu"));

    assertNotNull(targetPassport);
    assertTrue(passportRepository.existsById(targetPassport.getId()));
  }

  @Test
  public void testFindNotFoundShouldBeCreated() {
    var targetPassport =
        passportService.findOrCreateByYandexId(
            createUser(123L, "Денис", "Войтенко", "voitenko.da@phystech.edu"));

    assertNotNull(targetPassport);
    assertTrue(passportRepository.existsById(targetPassport.getId()));

    targetPassport = passportRepository.findByYandexId(123L);

    assertEquals("Денис", targetPassport.getFirstName());
    assertEquals("Войтенко", targetPassport.getLastName());
    assertEquals("voitenko.da@phystech.edu", targetPassport.getEmail());
  }

  private OAuth2User createUser(Long yandexId, String firstName, String lastName, String email) {
    var attrs = new HashMap<String, Object>();

    attrs.put("id", yandexId.toString());
    attrs.put("default_email", email);
    attrs.put("first_name", firstName);
    attrs.put("last_name", lastName);

    return new DefaultOAuth2User(new ArrayList<>(), attrs, "id");
  }
}
