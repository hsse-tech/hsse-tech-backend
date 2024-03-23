package com.mipt.hsse.hssetechbackend.rent.services;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = {"com.mipt.hsse.hssetechbackend.rent.services"})
class ItemServiceTest extends DatabaseSuite {
  @Autowired private ItemService itemService;
  @Autowired private JpaItemTypeRepository itemTypeRepository;

  @AfterEach
  void clear() {
    itemTypeRepository.deleteAll();
  }

  @Test
  void testCreateItem() {
    final String name = "Item type name";
    final BigDecimal cost = BigDecimal.valueOf(100);

    var createItemTypeRequest = new CreateItemTypeRequest(cost, name, 60, false);

    ItemType itemType = itemService.createItemType(createItemTypeRequest);

    ItemType extractedItemType = itemTypeRepository.findById(itemType.getId()).orElseThrow();

    assertEquals(0, cost.compareTo(extractedItemType.getCost()));
    assertEquals(extractedItemType.getDisplayName(), name);
  }

  @Test
  void testFailCreateItemWithNotUniqueName() {
    final String name = "Item type name";

    var request1 = new CreateItemTypeRequest(BigDecimal.ZERO, name, 60, false);
    var request2 = new CreateItemTypeRequest(BigDecimal.valueOf(100), name, 120, true);

    itemService.createItemType(request1);

    assertThrows(DataIntegrityViolationException.class, () -> itemService.createItemType(request2));
  }

  @Test
  void testCreateItemWithUndefinedMaxRentTime() {
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
}
