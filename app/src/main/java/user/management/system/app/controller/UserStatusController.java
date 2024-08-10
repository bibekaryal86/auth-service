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
import user.management.system.app.model.dto.ResponseCrudInfo;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.dto.UserStatusDto;
import user.management.system.app.model.dto.UserStatusRequest;
import user.management.system.app.model.dto.UserStatusResponse;
import user.management.system.app.model.entity.UserStatusEntity;
import user.management.system.app.service.UserStatusService;

@RestController
@RequestMapping("/user_status")
public class UserStatusController {

  private final UserStatusService userStatusService;

  public UserStatusController(final UserStatusService userStatusService) {
    this.userStatusService = userStatusService;
  }

  @PostMapping
  public ResponseEntity<UserStatusResponse> insertUserStatus(
      @RequestBody final UserStatusRequest userStatusRequest) {
    try {
      UserStatusEntity userStatusEntity = userStatusService.createUserStatus(userStatusRequest);
      return getResponseSingle(userStatusEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping
  public ResponseEntity<UserStatusResponse> getUserStatuses() {
    try {
      List<UserStatusEntity> userStatusEntities = userStatusService.retrieveUserStatuses();
      return getResponseMultiple(userStatusEntities);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserStatusResponse> getUserStatus(@PathVariable final int id) {
    try {
      UserStatusEntity userStatusEntity = userStatusService.retrieveUserStatusById(id);
      return getResponseSingle(userStatusEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserStatusResponse> modifyUserStatus(
      @PathVariable int id, @RequestBody final UserStatusRequest userStatusRequest) {
    try {
      UserStatusEntity userStatusEntity = userStatusService.updateUserStatus(id, userStatusRequest);
      return getResponseSingle(userStatusEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<UserStatusResponse> removeUserStatus(@PathVariable final int id) {
    try {
      userStatusService.deleteUserStatus(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  private ResponseEntity<UserStatusResponse> getResponseSingle(
      final UserStatusEntity userStatusEntity) {
    HttpStatus httpStatus = getHttpStatusForSingleResponse(userStatusEntity);
    ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(userStatusEntity);
    List<UserStatusDto> userStatusDtos =
        userStatusEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDto(userStatusEntity));
    return new ResponseEntity<>(
        new UserStatusResponse(userStatusDtos, null, null, responseStatusInfo), httpStatus);
  }

  private ResponseEntity<UserStatusResponse> getResponseMultiple(
      final List<UserStatusEntity> userStatusEntities) {
    List<UserStatusDto> userStatusDtos = convertEntitiesToDtos(userStatusEntities);
    return ResponseEntity.ok(new UserStatusResponse(userStatusDtos, null, null, null));
  }

  private ResponseEntity<UserStatusResponse> getResponseDelete() {
    return ResponseEntity.ok(
        new UserStatusResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  private ResponseEntity<UserStatusResponse> getResponseError(final Exception exception) {
    HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new UserStatusResponse(Collections.emptyList(), null, null, responseStatusInfo),
        httpStatus);
  }

  private UserStatusDto convertEntityToDto(final UserStatusEntity userStatusEntity) {
    if (userStatusEntity == null) {
      return null;
    }
    return new UserStatusDto(
        userStatusEntity.getId(), userStatusEntity.getName(), userStatusEntity.getDescription());
  }

  private List<UserStatusDto> convertEntitiesToDtos(
      final List<UserStatusEntity> userStatusEntities) {
    if (CollectionUtils.isEmpty(userStatusEntities)) {
      return Collections.emptyList();
    }
    return userStatusEntities.stream().map(this::convertEntityToDto).toList();
  }
}
