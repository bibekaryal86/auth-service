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
import user.management.system.app.model.dto.ProjectStatusDto;
import user.management.system.app.model.dto.ProjectStatusRequest;
import user.management.system.app.model.dto.ProjectStatusResponse;
import user.management.system.app.model.dto.ResponseCrudInfo;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.ProjectStatusEntity;
import user.management.system.app.service.ProjectStatusService;

@RestController
@RequestMapping("/project_status")
public class ProjectStatusController {

  private final ProjectStatusService projectStatusService;

  public ProjectStatusController(final ProjectStatusService projectStatusService) {
    this.projectStatusService = projectStatusService;
  }

  @PostMapping
  public ResponseEntity<ProjectStatusResponse> insertProjectStatus(
      @RequestBody final ProjectStatusRequest projectStatusRequest) {
    try {
      ProjectStatusEntity projectStatusEntity =
          projectStatusService.createProjectStatus(projectStatusRequest);
      return getResponseSingle(projectStatusEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping
  public ResponseEntity<ProjectStatusResponse> getProjectStatuses() {
    try {
      List<ProjectStatusEntity> projectStatusEntities =
          projectStatusService.retrieveAllProjectStatuses();
      return getResponseMultiple(projectStatusEntities);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectStatusResponse> getProjectStatus(@PathVariable final int id) {
    try {
      ProjectStatusEntity projectStatusEntity = projectStatusService.retrieveProjectStatusById(id);
      return getResponseSingle(projectStatusEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProjectStatusResponse> modifyProjectStatus(
      @PathVariable int id, @RequestBody final ProjectStatusRequest projectStatusRequest) {
    try {
      ProjectStatusEntity projectStatusEntity =
          projectStatusService.updateProjectStatus(id, projectStatusRequest);
      return getResponseSingle(projectStatusEntity);
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ProjectStatusResponse> removeProjectStatus(@PathVariable final int id) {
    try {
      projectStatusService.deleteProjectStatus(id);
      return getResponseDelete();
    } catch (Exception ex) {
      return getResponseError(ex);
    }
  }

  private ResponseEntity<ProjectStatusResponse> getResponseSingle(
      final ProjectStatusEntity projectStatusEntity) {
    HttpStatus httpStatus = getHttpStatusForSingleResponse(projectStatusEntity);
    ResponseStatusInfo responseStatusInfo =
        getResponseStatusInfoForSingleResponse(projectStatusEntity);
    List<ProjectStatusDto> projectStatusDtos =
        projectStatusEntity == null
            ? Collections.emptyList()
            : List.of(convertEntityToDto(projectStatusEntity));
    return new ResponseEntity<>(
        new ProjectStatusResponse(projectStatusDtos, null, null, responseStatusInfo), httpStatus);
  }

  private ResponseEntity<ProjectStatusResponse> getResponseMultiple(
      final List<ProjectStatusEntity> projectStatusEntities) {
    List<ProjectStatusDto> projectStatusDtos = convertEntitiesToDtos(projectStatusEntities);
    return ResponseEntity.ok(new ProjectStatusResponse(projectStatusDtos, null, null, null));
  }

  private ResponseEntity<ProjectStatusResponse> getResponseDelete() {
    return ResponseEntity.ok(
        new ProjectStatusResponse(
            Collections.emptyList(),
            ResponseCrudInfo.builder().deletedRowsCount(1).build(),
            null,
            null));
  }

  private ResponseEntity<ProjectStatusResponse> getResponseError(final Exception exception) {
    HttpStatus httpStatus = getHttpStatusForErrorResponse(exception);
    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg(exception.getMessage()).build();
    return new ResponseEntity<>(
        new ProjectStatusResponse(Collections.emptyList(), null, null, responseStatusInfo),
        httpStatus);
  }

  private ProjectStatusDto convertEntityToDto(final ProjectStatusEntity projectStatusEntity) {
    if (projectStatusEntity == null) {
      return null;
    }
    return new ProjectStatusDto(
        projectStatusEntity.getId(),
        projectStatusEntity.getName(),
        projectStatusEntity.getDescription());
  }

  private List<ProjectStatusDto> convertEntitiesToDtos(
      final List<ProjectStatusEntity> projectStatusEntities) {
    if (CollectionUtils.isEmpty(projectStatusEntities)) {
      return Collections.emptyList();
    }
    return projectStatusEntities.stream().map(this::convertEntityToDto).toList();
  }
}
