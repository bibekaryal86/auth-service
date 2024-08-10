package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getHttpStatusForErrorResponse;
import static user.management.system.app.util.CommonUtils.getHttpStatusForSingleResponse;
import static user.management.system.app.util.CommonUtils.getResponseStatusInfoForSingleResponse;

import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.dto.AddressTypeDto;
import user.management.system.app.model.dto.AddressTypeRequest;
import user.management.system.app.model.dto.AddressTypeResponse;
import user.management.system.app.model.dto.ResponseCrudInfo;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AddressTypeEntity;
import user.management.system.app.service.AddressTypeService;

@RestController
@RequestMapping("/address_type")
public class AddressTypeController {

  private final AddressTypeService addressTypeService;

  public AddressTypeController(final AddressTypeService addressTypeService) {
    this.addressTypeService = addressTypeService;
  }

  @PostMapping
  public ResponseEntity<AddressTypeResponse> insertAddressType(
      @RequestBody final AddressTypeRequest addressTypeRequest) {
    try {
      AddressTypeEntity addressTypeEntity =
          addressTypeService.createAddressType(addressTypeRequest);
      return getResponseSingle(addressTypeEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping
  public ResponseEntity<AddressTypeResponse> getAddressTypes() {
    try {
      List<AddressTypeEntity> addressTypeEntities = addressTypeService.retrieveAddressTypes();
      return getResponseMultiple(addressTypeEntities);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<AddressTypeResponse> getAddressType(@PathVariable final int id) {
    try {
      AddressTypeEntity addressTypeEntity = addressTypeService.retrieveAddressTypeById(id);
      return getResponseSingle(addressTypeEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<AddressTypeResponse> modifyAddressType(
      @PathVariable int id, @RequestBody final AddressTypeRequest addressTypeRequest) {
    try {
      AddressTypeEntity addressTypeEntity =
          addressTypeService.updateAddressType(id, addressTypeRequest);
      return getResponseSingle(addressTypeEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<AddressTypeResponse> removeAddressType(@PathVariable final int id) {
    try {
      addressTypeService.deleteAddressType(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  private ResponseEntity<AddressTypeResponse> getResponseSingle(
      final AddressTypeEntity addressTypeEntity) {
    HttpStatus httpStatus = getHttpStatusForSingleResponse(addressTypeEntity);
    ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(addressTypeEntity);
    List<AddressTypeDto> addressTypeDtos =
        addressTypeEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDto(addressTypeEntity));
    return new ResponseEntity<>(
        new AddressTypeResponse(addressTypeDtos, null, null, responseStatusInfo), httpStatus);
  }

  private ResponseEntity<AddressTypeResponse> getResponseMultiple(
      final List<AddressTypeEntity> addressTypeEntities) {
    List<AddressTypeDto> addressTypeDtos = convertEntitiesToDtos(addressTypeEntities);
    return ResponseEntity.ok(new AddressTypeResponse(addressTypeDtos, null, null, null));
  }

  private ResponseEntity<AddressTypeResponse> getResponseDelete() {
    return ResponseEntity.ok(
        new AddressTypeResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  private ResponseEntity<AddressTypeResponse> getResponseError(final Exception exception) {
    HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new AddressTypeResponse(Collections.emptyList(), null, null, responseStatusInfo),
        httpStatus);
  }

  private AddressTypeDto convertEntityToDto(final AddressTypeEntity addressTypeEntity) {
    if (addressTypeEntity == null) {
      return null;
    }
    return new AddressTypeDto(
        addressTypeEntity.getId(), addressTypeEntity.getName(), addressTypeEntity.getDescription());
  }

  private List<AddressTypeDto> convertEntitiesToDtos(
      final List<AddressTypeEntity> addressTypeEntities) {
    if (CollectionUtils.isEmpty(addressTypeEntities)) {
      return Collections.emptyList();
    }
    return addressTypeEntities.stream().map(this::convertEntityToDto).toList();
  }
}
