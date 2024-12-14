package auth.service.app.controller;

import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.dto.StatusTypeRequest;
import auth.service.app.model.dto.StatusTypeResponse;
import auth.service.app.model.entity.StatusTypeEntity;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.StatusTypeService;
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
@RequestMapping("/api/v1/status_types")
@Validated
public class StatusTypeController {

  private final StatusTypeService statusTypeService;
  private final CircularDependencyService circularDependencyService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @CheckPermission("STATUS_TYPE_CREATE")
  @PostMapping("/status_type")
  public ResponseEntity<StatusTypeResponse> createStatusType(
      @Valid @RequestBody final StatusTypeRequest statusTypeRequest) {
    try {
      final StatusTypeEntity statusTypeEntity =
          statusTypeService.createStatusType(statusTypeRequest);
      return entityDtoConvertUtils.getResponseSingleStatusType(statusTypeEntity);
    } catch (Exception ex) {
      log.error("Create Status Type: [{}]", statusTypeRequest, ex);
      return entityDtoConvertUtils.getResponseErrorStatusType(ex);
    }
  }

  @CheckPermission("STATUS_TYPE_READ")
  @GetMapping
  public ResponseEntity<StatusTypeResponse> readStatusTypes() {
    try {
      final List<StatusTypeEntity> statusTypeEntities = statusTypeService.readStatusTypes();
      return entityDtoConvertUtils.getResponseMultipleStatusTypes(statusTypeEntities);
    } catch (Exception ex) {
      log.error("Read Status Types...", ex);
      return entityDtoConvertUtils.getResponseErrorStatusType(ex);
    }
  }

  @CheckPermission("STATUS_TYPE_READ")
  @GetMapping("/status_type/{id}")
  public ResponseEntity<StatusTypeResponse> readStatusType(@PathVariable final long id) {
    try {
      final StatusTypeEntity statusTypeEntity = circularDependencyService.readStatusType(id);
      return entityDtoConvertUtils.getResponseSingleStatusType(statusTypeEntity);
    } catch (Exception ex) {
      log.error("Read Status Type: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorStatusType(ex);
    }
  }

  @CheckPermission("STATUS_TYPE_UPDATE")
  @PutMapping("/status_type/{id}")
  public ResponseEntity<StatusTypeResponse> updateStatusType(
      @PathVariable final long id, @Valid @RequestBody final StatusTypeRequest statusTypeRequest) {
    try {
      final StatusTypeEntity statusTypeEntity =
          statusTypeService.updateStatusType(id, statusTypeRequest);
      return entityDtoConvertUtils.getResponseSingleStatusType(statusTypeEntity);
    } catch (Exception ex) {
      log.error("Update StatusType: [{}] | [{}]", id, statusTypeRequest, ex);
      return entityDtoConvertUtils.getResponseErrorStatusType(ex);
    }
  }

  @CheckPermission("STATUS_TYPE_DELETE")
  @DeleteMapping("/status_type/{id}")
  public ResponseEntity<StatusTypeResponse> softDeleteStatusType(@PathVariable final long id) {
    try {
      statusTypeService.softDeleteStatusType(id);
      return entityDtoConvertUtils.getResponseDeleteStatusType();
    } catch (Exception ex) {
      log.error("Soft Delete StatusType: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorStatusType(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/status_type/{id}/hard")
  public ResponseEntity<StatusTypeResponse> hardDeleteStatusType(@PathVariable final long id) {
    try {
      statusTypeService.hardDeleteStatusType(id);
      return entityDtoConvertUtils.getResponseDeleteStatusType();
    } catch (Exception ex) {
      log.error("Hard Delete StatusType: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorStatusType(ex);
    }
  }

  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/status_type/{id}/restore")
  public ResponseEntity<StatusTypeResponse> restoreStatusType(@PathVariable final long id) {
    try {
      final StatusTypeEntity statusTypeEntity = statusTypeService.restoreSoftDeletedStatusType(id);
      return entityDtoConvertUtils.getResponseSingleStatusType(statusTypeEntity);
    } catch (Exception ex) {
      log.error("Restore StatusType: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorStatusType(ex);
    }
  }
}
