package com.mipt.hsse.hssetechbackend.oauth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhystechDomainValidatorTest {
  @Test
  void testIsValidMustBeValid() {
    assertTrue(PhystechDomainValidator.isValid("voitenko.da@phystech.edu"));
    assertTrue(PhystechDomainValidator.isValid("fisman.ma@phystech.edu"));
    assertTrue(PhystechDomainValidator.isValid("furmanov.ma@phystech.edu"));
  }

  @Test
  void testIsValidMustBeInvalid() {
    assertFalse(PhystechDomainValidator.isValid("voitenko.da@google.com"));
    assertFalse(PhystechDomainValidator.isValid("fisman.ma@yandex.ru"));
    assertFalse(PhystechDomainValidator.isValid("furmanov.ma@outlook.com"));
    assertFalse(PhystechDomainValidator.isValid("furmanov.maoutlook.com"));
    assertFalse(PhystechDomainValidator.isValid("voitenko.da@phystech.eduedu"));
  }
}
