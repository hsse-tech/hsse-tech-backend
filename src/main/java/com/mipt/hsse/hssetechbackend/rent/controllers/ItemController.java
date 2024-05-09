package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.google.zxing.WriterException;
import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetItemResponse;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetShortRentResponse;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
    Item createdItem = itemService.createItem(request);
    return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
  }

  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateItem(
      @PathVariable("id") UUID itemId, @Valid @RequestBody UpdateItemRequest request) {
      itemService.updateItem(itemId, request);
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
      List<GetShortRentResponse> rentsResponses =
          rents.stream().map(GetShortRentResponse::getFromRent).toList();
      response = new GetItemResponse(item, rentsResponses);
    } else {
      response = new GetItemResponse(item);
    }

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Returns the shortened information about all items present.<br>
   * "Shortened" means that the information about the rents of the items does not get uploaded
   */
  @GetMapping()
  public ResponseEntity<List<GetItemResponse>> getAllItems() {
    List<Item> allItems = itemService.getAllItems();
    List<GetItemResponse> itemsResponses =  allItems.stream().map(GetItemResponse::new).toList();
    return ResponseEntity.ok(itemsResponses);
  }

  @GetMapping(value = "/{item_id}/qr", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody Resource getItemBookingQRCode(
      @PathVariable("item_id") UUID itemId,
      @Value("${item-qrcode-width}") int WIDTH,
      @Value("${item-qrcode-height}") int HEIGHT)
      throws IOException, WriterException {

    byte[] qrCodeBytes = itemService.getQrCodeForItem(itemId, WIDTH, HEIGHT);

    return new ByteArrayResource(qrCodeBytes);
  }

  @PostMapping("/{item_id}/try-open")
  public void provideAccessToItemIfAllowed(@PathVariable("item_id") UUID itemId) {
    if (!itemService.existsById(itemId)) throw new EntityNotFoundException();

    itemService.provideAccessToItem(itemId);
  }

  @DeleteMapping("/{itemId}")
  public void deleteItem(@PathVariable("itemId") UUID itemId) {
    itemService.deleteItem(itemId);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiError> entityNotFoundExceptionHandler(
      EntityNotFoundException ex) {
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    return RestExceptionHandler.buildResponseEntity(apiError);
  }
}
