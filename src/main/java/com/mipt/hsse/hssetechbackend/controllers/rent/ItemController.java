package com.mipt.hsse.hssetechbackend.controllers.rent;

import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.controllers.rent.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.controllers.rent.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.controllers.rent.responses.GetItemResponse;
import com.mipt.hsse.hssetechbackend.controllers.rent.responses.GetShortRentResponse;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoAlreadyExistsException;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import com.mipt.hsse.hssetechbackend.utils.PngUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/renting/item")
public class ItemController {
  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Item> createItem(@Valid @RequestBody CreateItemRequest request) {
    Item createdItem = itemService.createItem(request);
    return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
  }

  @PostMapping(value = "/{item_id}/photo", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> pinItemThumbnailPhoto(
      @PathVariable("item_id") UUID itemId, HttpServletRequest photoServletRequest)
      throws IOException {
    byte[] photoBytes = photoServletRequest.getInputStream().readAllBytes();

    if (!PngUtility.isPngFormat(photoBytes)) {
      return ResponseEntity.badRequest().build();
    }

    itemService.saveItemPhoto(itemId, photoBytes);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping(value = "/{item_id}/photo")
  public ResponseEntity<Resource> getItemThumbnailPhoto(@PathVariable("item_id") UUID itemId) {
    byte[] photoBytes = itemService.getItemPhoto(itemId);
    var returnResource = new ByteArrayResource(photoBytes);

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"thumbnail.png\"")
        .body(returnResource);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> updateItem(
      @PathVariable("id") UUID itemId, @Valid @RequestBody UpdateItemRequest request) {
    itemService.updateItem(itemId, request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{itemId}")
  public ResponseEntity<GetItemResponse> getItem(
      @PathVariable("itemId") UUID itemId,
      @RequestParam(value = "loadRentInfo", defaultValue = "false") boolean loadRentInfo) {
    Optional<Item> itemOpt = itemService.getItem(itemId);
    Item item = itemOpt.orElseThrow(() -> EntityNotFoundException.itemNotFound(itemId));

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
    List<GetItemResponse> itemsResponses = allItems.stream().map(GetItemResponse::new).toList();
    return ResponseEntity.ok(itemsResponses);
  }

  //  @GetMapping(value = "/{item_id}/qr", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  //  public @ResponseBody ResponseEntity<ByteArrayResource> getItemBookingQRCode(
  //      @PathVariable("item_id") UUID itemId,
  //      @Value("${item-qrcode-width}") int WIDTH,
  //      @Value("${item-qrcode-height}") int HEIGHT)
  //      throws IOException, WriterException {
  //
  //    byte[] qrCodeBytes = itemService.getQrCodeForItem(itemId, WIDTH, HEIGHT);
  //
  //    var resource = new ByteArrayResource(qrCodeBytes);

  //  TODO: compare with code in rent
  //    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
  //      .headers(HttpHeaders.CONTENT_DISPOSITION, "attachment;
  // filename=/"qrcode/"").body(resource);
  //  }

  @PostMapping("/{item_id}/try-open")
  public ResponseEntity<Void> provideAccessToItemIfAllowed(@PathVariable("item_id") UUID itemId) {
    if (!itemService.existsById(itemId)) throw EntityNotFoundException.itemNotFound(itemId);

    itemService.provideAccessToItem(itemId);
    return ResponseEntity.ok().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{itemId}")
  public ResponseEntity<Void> deleteItem(@PathVariable("itemId") UUID itemId) throws IOException {
    itemService.deleteItem(itemId);
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler({PhotoAlreadyExistsException.class, PhotoNotFoundException.class})
  public ResponseEntity<ApiError> exceptionHandler(Exception ex) {
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    return RestExceptionHandler.buildResponseEntity(apiError);
  }
}
