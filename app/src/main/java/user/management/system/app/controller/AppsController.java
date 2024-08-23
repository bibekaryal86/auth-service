package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppsRequest;
import user.management.system.app.model.dto.AppsResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.service.AppsService;
import user.management.system.app.service.AuditService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Apps Management")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/apps")
@Validated
public class AppsController {

  private final AppsService appsService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @Operation(
      summary = "Create a new app",
      description = "Creates a new app",
      security = @SecurityRequirement(name = "Token"),
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the app to create",
              content = @Content(schema = @Schema(implementation = AppsRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "App created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Required Element Missing",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN CREATE APP")
  @PostMapping("/app")
  public ResponseEntity<AppsResponse> createApp(
      @Valid @RequestBody final AppsRequest appsRequest, final HttpServletRequest request) {
    try {
      final AppsEntity appsEntity = appsService.createApp(appsRequest);
      auditService.auditAppsCreate(request, appsEntity);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      log.error("Create App: [{}]", appsRequest, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @Operation(
      summary = "Get all apps",
      description = "Retrieves all apps available in the system",
      security = @SecurityRequirement(name = "Token"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Apps retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN READ APP")
  @GetMapping
  public ResponseEntity<AppsResponse> readApps() {
    try {
      final List<AppsEntity> appsEntities = appsService.readApps();
      return entityDtoConvertUtils.getResponseMultipleApps(appsEntities);
    } catch (Exception ex) {
      log.error("Read Apps...", ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @Operation(
      summary = "Get app by ID",
      description = "Retrieves a specific app by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app to retrieve", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "App retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - App Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN READ APP")
  @GetMapping("/app/{id}")
  public ResponseEntity<AppsResponse> readApp(@PathVariable final String id) {
    try {
      final AppsEntity appsEntity = appsService.readApp(id);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      log.error("Read App: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @Operation(
      summary = "Update an app by ID",
      description = "Updates an existing app identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app to update", required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the app to update",
              required = true,
              content = @Content(schema = @Schema(implementation = AppsRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "App updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Required Element Missing",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - App Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN UPDATE APP")
  @PutMapping("/app/{id}")
  public ResponseEntity<AppsResponse> updateApp(
      @PathVariable final String id,
      @Valid @RequestBody final AppsRequest appsRequest,
      final HttpServletRequest request) {
    try {
      final AppsEntity appsEntity = appsService.updateApps(id, appsRequest);
      auditService.auditAppsUpdate(request, appsEntity);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      log.error("Update App: [{}] | [{}]", id, appsRequest, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @Operation(
      summary = "Soft delete an app by ID",
      description =
          "Marks an app as deleted without permanently removing it, identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app to soft delete", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "App soft deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - App Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN DELETE APP")
  @DeleteMapping("/app/{id}")
  public ResponseEntity<AppsResponse> softDeleteApp(
      @PathVariable final String id, final HttpServletRequest request) {
    try {
      appsService.softDeleteApps(id);
      auditService.auditAppsDeleteSoft(request, id);
      return entityDtoConvertUtils.getResponseDeleteApps();
    } catch (Exception ex) {
      log.error("Soft Delete App: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @Operation(
      summary = "Hard delete an app by ID",
      description = "Permanently removes an app identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app to hard delete", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "App hard deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - App Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/app/{id}/hard")
  public ResponseEntity<AppsResponse> hardDeleteApp(
      @PathVariable final String id, final HttpServletRequest request) {
    try {
      appsService.hardDeleteApps(id);
      auditService.auditAppsDeleteHard(request, id);
      return entityDtoConvertUtils.getResponseDeleteApps();
    } catch (Exception ex) {
      log.error("Hard Delete App: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }

  @Operation(
      summary = "Restore a soft-deleted app by ID",
      description = "Restores an app that was previously soft-deleted, identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app to restore", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "App restored successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - App Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/app/{id}/restore")
  public ResponseEntity<AppsResponse> restoreApp(
      @PathVariable final String id, final HttpServletRequest request) {
    try {
      final AppsEntity appsEntity = appsService.restoreSoftDeletedApps(id);
      auditService.auditAppsRestore(request, id);
      return entityDtoConvertUtils.getResponseSingleApps(appsEntity);
    } catch (Exception ex) {
      log.error("Restore App: [{}]", id, ex);
      return entityDtoConvertUtils.getResponseErrorApps(ex);
    }
  }
}
