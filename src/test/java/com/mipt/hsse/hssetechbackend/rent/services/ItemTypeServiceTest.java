package com.mipt.hsse.hssetechbackend.rent.services;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import(ItemService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemTypeServiceTest extends DatabaseSuite {
  @Autowired private ItemService itemService;
  @Autowired private JpaItemTypeRepository itemTypeRepository;

  @AfterEach
  void clear() {
    itemTypeRepository.deleteAll();
  }

  @Test
  void testCreateItemType() {
    final String name = "Item type name";
    final BigDecimal cost = BigDecimal.valueOf(100);

    var createItemTypeRequest = new CreateItemTypeRequest(cost, name, 60, false);

    ItemType itemType = itemService.createItemType(createItemTypeRequest);

    ItemType extractedItemType = itemTypeRepository.findById(itemType.getId()).orElseThrow();

    assertEquals(0, cost.compareTo(extractedItemType.getCost()));
    assertEquals(extractedItemType.getDisplayName(), name);
  }

  @Test
  void testFailCreateItemTypeWithNotUniqueName() {
    final String name = "Item type name";

    var request1 = new CreateItemTypeRequest(BigDecimal.ZERO, name, 60, false);
    var request2 = new CreateItemTypeRequest(BigDecimal.valueOf(100), name, 120, true);

    itemService.createItemType(request1);

    assertThrows(DataIntegrityViolationException.class, () -> itemService.createItemType(request2));
  }

  @Test
  void testCreateItemTypeWithUndefinedMaxRentTime() {
    final String name = "Item type name";

    var createRequest = new CreateItemTypeRequest(BigDecimal.ZERO, name, null, false);

    ItemType itemType = itemService.createItemType(createRequest);

    ItemType extractedItemType = itemTypeRepository.findById(itemType.getId()).orElseThrow();

    assertNull(extractedItemType.getMaxRentTimeMinutes());
  }

  @Test
  void testFailCreateItemTypeWithNegativeCost() {
    final String name = "Item type name";

    var createRequest = new CreateItemTypeRequest(BigDecimal.valueOf(-1), name, null, false);

    assertThrows(TransactionSystemException.class, () -> itemService.createItemType(createRequest));
  }
  
  @Test
  void testDeleteItemType() {
    final String name = "Item type name";
    final BigDecimal cost = BigDecimal.valueOf(100);

    var createItemTypeRequest = new CreateItemTypeRequest(cost, name, 60, false);
    ItemType itemType = itemService.createItemType(createItemTypeRequest);

    itemService.deleteItemType(itemType.getId());
    
    assertTrue(itemTypeRepository.findById(itemType.getId()).isEmpty());
  }

  @Test
  void testFailDeleteAbsentItemType() {
    final UUID id = UUID.randomUUID();
    
    assertThrows(EntityNotFoundException.class, () -> itemService.deleteItemType(id));
  }
}
