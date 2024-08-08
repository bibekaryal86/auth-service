package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.service.ProjectService;

@RestController
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Operation(summary = "Retrieve All Projects")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Projects found"),
        @ApiResponse(responseCode = "204", description = "Projects not found"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @GetMapping
  public ResponseEntity<ProjectResponse> getAllProjects(
      @RequestParam(required = false, defaultValue = "25") Integer limit,
      @RequestParam(required = false, defaultValue = "0") Integer offset,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedProjects,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedUsers,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedRoles) {
    try {
      List<Project> projects =
          projectService.getAllProjects(
              limit, offset, includeDeletedProjects, includeDeletedUsers, includeDeletedRoles);

      if (projects.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return ResponseEntity.ok(ProjectResponse.builder().projects(projects).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          ProjectResponse.builder().error(e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Retrieve One Project By ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Project found"),
        @ApiResponse(responseCode = "404", description = "Project not found by provided id"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @GetMapping(value = "/{id}")
  public ResponseEntity<ProjectResponse> getProjectById(
      @PathVariable("id") Integer id,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedProjects,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedUsers,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedRoles) {
    try {
      Project project =
          projectService.getProjectById(
              id, includeDeletedProjects, includeDeletedUsers, includeDeletedRoles);
      if (project == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return ResponseEntity.ok(
          ProjectResponse.builder().projects(Collections.singletonList(project)).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          ProjectResponse.builder().error(e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Create One Project")
  @PostMapping
  public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest project) {
    try {
      int newId = projectService.createProject(project);
      if (newId <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return new ResponseEntity<>(
          ProjectResponse.builder().createdRowsId(newId).build(), HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(
          ProjectResponse.builder().error(e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Update One Project")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Project successfully updated"),
        @ApiResponse(responseCode = "404", description = "Project not found by provided id"),
        @ApiResponse(responseCode = "503", description = "Project was not updated"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @PutMapping(value = "/{id}")
  public ResponseEntity<ProjectResponse> updateProject(
      @PathVariable("id") Integer id, @RequestBody ProjectRequest project) {
    try {
      Project currentProject = projectService.getProjectById(id, true, true, true);
      if (currentProject == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int updatedRowsCount = projectService.updateProject(id, project);
      if (updatedRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(
          ProjectResponse.builder().updatedRowsCount(updatedRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          ProjectResponse.builder().error(e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Delete One Project, Requires SuperUser Permissions for Hard Delete")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Project successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Project not found by provided id"),
        @ApiResponse(responseCode = "503", description = "Project was not deleted"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @DeleteMapping(value = "/{id}")
  public ResponseEntity<ProjectResponse> deleteProject(
      @PathVariable("id") Integer id,
      @RequestParam(required = false, defaultValue = "false") Boolean isHardDelete) {
    try {
      Project currentProject = projectService.getProjectById(id, true, true, true);
      if (currentProject == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int deletedRowsCount = projectService.deleteProject(id, isHardDelete);
      if (deletedRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(
          ProjectResponse.builder().deletedRowsCount(deletedRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          ProjectResponse.builder().error(e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Restore One Soft Deleted Project, Requires SuperUser Permissions")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Project successfully restored"),
        @ApiResponse(responseCode = "404", description = "Project not found by provided id"),
        @ApiResponse(responseCode = "503", description = "Project was not restored"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @PatchMapping(value = "/{id}")
  public ResponseEntity<ProjectResponse> restoreProject(@PathVariable("id") Integer id) {
    try {
      Project currentProject = projectService.getProjectById(id, true, true, true);
      if (currentProject == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int restoredRowsCount = projectService.restoreProject(id);
      if (restoredRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(
          ProjectResponse.builder().deletedRowsCount(restoredRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          ProjectResponse.builder().error(e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
