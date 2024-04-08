package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetItemResponse;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetRentResponse;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.qrcodegeneration.QrCodeManager;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import jakarta.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/renting/item")
public class ItemController {
  private final ItemService itemService;

  // TODO: Lock service is not implemented yet
  // private final LockService lockService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping
  public ResponseEntity<Item> createItem(@Valid @RequestBody CreateItemRequest request) {
    try {
      Item createdItem = itemService.createItem(request);
      return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    } catch (EntityNotFoundException e) {
      throw new EntityNotFoundException("Expected item type does not exist");
    }
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateItem(
      @PathVariable("id") UUID itemId, @Valid @RequestBody UpdateItemRequest request) {
    if (itemService.existsById(itemId)) {
      itemService.updateItem(itemId, request);
      return ResponseEntity.noContent().build();
    } else {
      throw new EntityNotFoundException();
    }
  }

  @GetMapping("/{itemId}")
  public ResponseEntity<GetItemResponse> getItem(
      @PathVariable("itemId") UUID itemId,
      @RequestParam(value = "loadRentInfo", defaultValue = "false") boolean loadRentInfo) {
    Optional<Item> itemOpt = itemService.getItem(itemId);
    Item item = itemOpt.orElseThrow(EntityNotFoundException::new);

    GetItemResponse response;
    if (loadRentInfo) {
      List<Rent> rents = itemService.getFutureRentsOfItem(itemId);
      List<GetRentResponse> rentsResponses =
          rents.stream().map(GetRentResponse::getFromRent).toList();
      response = new GetItemResponse(item, rentsResponses);
    } else {
      response = new GetItemResponse(item);
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping(value = "/{item_id}/qr")
  public ResponseEntity<byte[]> getItemBookingQRCode(@PathVariable("item_id") UUID itemId) {
    final int WIDTH = 200;
    final int HEIGHT = 200;

    BitMatrix qrCodeMatrix;
    try {
      // TODO: When we have domain, it should be put in here
      qrCodeMatrix = QrCodeManager.createQR("https://{DOMAIN}/rent/" + itemId, HEIGHT, WIDTH);
    } catch (WriterException e) {
      throw new RuntimeException(e);
    }
    BufferedImage image = MatrixToImageWriter.toBufferedImage(qrCodeMatrix);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", byteArrayOutputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    byte[] imageInBytes = byteArrayOutputStream.toByteArray();

    return ResponseEntity.status(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"image.png\"")
        .contentType(MediaType.IMAGE_PNG)
        .body(imageInBytes);
  }

  @PostMapping("/{item_id}/try-open")
  public void provideAccessToItemIfAllowed(@PathVariable("item_id") UUID itemId) {
    if (!itemService.existsById(itemId)) throw new EntityNotFoundException();

    UUID lockId = itemService.getItemLockId(itemId);

    // TODO: Lock service is not implemented yet
    throw new UnsupportedOperationException("Lock service is not implemented yet");
    //    if (!lockService.existsById(lockId))
    //      throw new EntityNotFoundException("The lock that is assigned to this item does not
    // exist");
    //
    //    lockService.requireOpenById(lockId);
  }

  @DeleteMapping("/{itemId}")
  public void deleteItem(@PathVariable("itemId") UUID itemId) {
    itemService.deleteItem(itemId);
  }

  @ExceptionHandler
  public ResponseEntity<ClientServerError> entityNotFoundExceptionHandler(
      EntityNotFoundException e) {
    return new ResponseEntity<>(new ClientServerError(e.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
