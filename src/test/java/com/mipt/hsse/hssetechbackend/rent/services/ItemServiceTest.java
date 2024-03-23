package com.mipt.hsse.hssetechbackend.rent.services;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = {"com.mipt.hsse.hssetechbackend.rent.services"})
class ItemServiceTest extends DatabaseSuite {
  @Autowired private ItemService itemService;

  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaItemRepository itemRepository;

  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "Item type name", 60, false);

  @BeforeEach
  void save() {
    itemTypeRepository.save(itemType);
  }

  @AfterEach
  void clear() {
    itemTypeRepository.deleteAll();
    itemRepository.deleteAll();
  }

  @Test
  void testCreateItem() {
    final String itemName = "Particular item name";
    var createItemRequest = new CreateItemRequest(itemName, itemType);

    Item item = itemService.createItem(createItemRequest);

    Item extractedItem = itemRepository.findById(item.getId()).orElseThrow();
    assertEquals(itemName, extractedItem.getDisplayName());
    assertEquals(itemType.getId(), extractedItem.getType().getId());
  }
}
