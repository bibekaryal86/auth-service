package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.AddressTypeRequest;
import auth.service.app.model.dto.AddressTypeResponse;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.service.AddressTypeService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.util.EntityDtoConvertUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/address_types")
@Validated
public class AddressTypeController {

  private final AddressTypeService addressTypeService;
  private final CircularDependencyService circularDependencyService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @CheckPermission("STATUS_TYPE_CREATE")
  @PostMapping("/address_type")
  public ResponseEntity<AddressTypeResponse> createAddressType(
      @Valid @RequestBody final AddressTypeRequest addressTypeRequest) {
    try {
      final AddressTypeEntity addressTypeEntity =
          addressTypeService.createAddressType(addressTypeRequest);
      return entityDtoConvertUtils.getResponseSingleAddressType(addressTypeEntity);
    } catch (Exception ex) {
      log.error("Create Address Type: [{}]", addressTypeRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAddressType(ex);
    }
  }

  @CheckPermission("STATUS_TYPE_READ")
  @GetMapping
  public ResponseEntity<AddressTypeResponse> readAddressTypes() {
    try {
      final List<AddressTypeEntity> addressTypeEntities = addressTypeService.readAddressTypes();
      return entityDtoConvertUtils.getResponseMultipleAddressTypes(addressTypeEntities);
    } catch (Exception ex) {
      log.error("Read Address Types...", ex);
      return entityDtoConvertUtils.getResponseErrorAddressType(ex);
    }
  }

  @CheckPermission("STATUS_TYPE_READ")
  @GetMapping("/address_type/{id}")
  public ResponseEntity<AddressTypeResponse> readAddressType(@PathVariable final long id) {
    try {
      final AddressTypeEntity addressTypeEntity = circularDependencyService.readAddressType(id);
      return entityDtoConvertUtils.getResponseSingleAddressType(addressTypeEntity);
    } catch (Exception ex) {
      log.error("Read Address Type: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAddressType(ex);
    }
  }

  @CheckPermission("STATUS_TYPE_UPDATE")
  @PutMapping("/address_type/{id}")
  public ResponseEntity<AddressTypeResponse> updateAddressType(
      @PathVariable final long id,
      @Valid @RequestBody final AddressTypeRequest addressTypeRequest) {
    try {
      final AddressTypeEntity addressTypeEntity =
          addressTypeService.updateAddressType(id, addressTypeRequest);
      return entityDtoConvertUtils.getResponseSingleAddressType(addressTypeEntity);
    } catch (Exception ex) {
      log.error("Update Address Type: [{}] | [{}]", id, addressTypeRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAddressType(ex);
    }
  }

  @CheckPermission("STATUS_TYPE_DELETE")
  @DeleteMapping("/address_type/{id}")
  public ResponseEntity<AddressTypeResponse> softDeleteAddressType(@PathVariable final long id) {
    try {
      addressTypeService.softDeleteAddressType(id);
      return entityDtoConvertUtils.getResponseDeleteAddressType();
    } catch (Exception ex) {
      log.error("Soft Delete Address Type: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAddressType(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/address_type/{id}/hard")
  public ResponseEntity<AddressTypeResponse> hardDeleteAddressType(@PathVariable final long id) {
    try {
      addressTypeService.hardDeleteAddressType(id);
      return entityDtoConvertUtils.getResponseDeleteAddressType();
    } catch (Exception ex) {
      log.error("Hard Delete Address Type: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAddressType(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/address_type/{id}/restore")
  public ResponseEntity<AddressTypeResponse> restoreAddressType(@PathVariable final long id) {
    try {
      final AddressTypeEntity addressTypeEntity =
          addressTypeService.restoreSoftDeletedAddressType(id);
      return entityDtoConvertUtils.getResponseSingleAddressType(addressTypeEntity);
    } catch (Exception ex) {
      log.error("Restore Address Type: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorAddressType(ex);
    }
  }
}
