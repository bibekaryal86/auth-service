package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import user.management.system.app.model.dto.AppPermissionRequest;
import user.management.system.app.model.dto.AppPermissionResponse;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.service.AppPermissionService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Permissions Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_permissions")
public class AppPermissionController {

  private final AppPermissionService appPermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @Operation(
      summary = "Create a new permission for an application",
      description = "Creates a new permission for the specified application ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which the permission is created",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the permission to create",
              content = @Content(schema = @Schema(implementation = AppPermissionRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Required Element Missing",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class)))
      })
  @CheckPermission("PERMISSION_CREATE")
  @PostMapping("/{appId}/permission")
  public ResponseEntity<AppPermissionResponse> createAppPermission(
      @PathVariable final String appId,
      @RequestBody final AppPermissionRequest appPermissionRequest) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.createAppPermission(appId, appPermissionRequest);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @Operation(
      summary = "Get all permissions",
      description = "Retrieves all permissions available in the system",
      security = @SecurityRequirement(name = "Token"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class)))
      })
  @CheckPermission("PERMISSION_READ")
  @GetMapping
  public ResponseEntity<AppPermissionResponse> readAppPermissions() {
    try {
      final List<AppPermissionEntity> appPermissionEntities =
          appPermissionService.readAppPermissions();
      return entityDtoConvertUtils.getResponseMultipleAppPermission(appPermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @Operation(
      summary = "Get permissions by application ID",
      description = "Retrieves all permissions associated with the specified application ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which permissions are retrieved",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class)))
      })
  @CheckPermission("PERMISSION_READ")
  @GetMapping("/app/{appId}")
  public ResponseEntity<AppPermissionResponse> readAppPermissionsByAppName(
      @PathVariable final String appId) {
    try {
      final List<AppPermissionEntity> appPermissionEntities =
          appPermissionService.readAppPermissions(appId);
      return entityDtoConvertUtils.getResponseMultipleAppPermission(appPermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @Operation(
      summary = "Get permission by ID",
      description = "Retrieves a specific permission by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the permission to retrieve", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Permission Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class)))
      })
  @CheckPermission("PERMISSION_READ")
  @GetMapping("/permission/{id}")
  public ResponseEntity<AppPermissionResponse> readAppPermission(@PathVariable final int id) {
    try {
      final AppPermissionEntity appPermissionEntity = appPermissionService.readAppPermission(id);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @Operation(
      summary = "Update a permission by ID",
      description = "Updates an existing permission identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the permission to update", required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the permission to update",
              required = true,
              content = @Content(schema = @Schema(implementation = AppPermissionRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Permission Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class)))
      })
  @CheckPermission("PERMISSION_UPDATE")
  @PutMapping("/permission/{id}")
  public ResponseEntity<AppPermissionResponse> updateAppPermission(
      @PathVariable final int id, @RequestBody final AppPermissionRequest appPermissionRequest) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.updateAppPermission(id, appPermissionRequest);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @Operation(
      summary = "Soft delete a permission by ID",
      description =
          "Marks a permission as deleted without permanently removing it, identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "id",
            description = "ID of the permission to soft delete",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission soft deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Permission Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class)))
      })
  @CheckPermission("PERMISSION_DELETE")
  @DeleteMapping("/permission/{id}")
  public ResponseEntity<AppPermissionResponse> softDeleteAppPermission(@PathVariable final int id) {
    try {
      appPermissionService.softDeleteAppPermission(id);
      return entityDtoConvertUtils.getResponseDeleteAppPermission();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @Operation(
      summary = "Hard delete a permission by ID",
      description = "Permanently removes a permission identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "id",
            description = "ID of the permission to hard delete",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission hard deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Permission Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/permission/{id}/hard")
  public ResponseEntity<AppPermissionResponse> hardDeleteAppPermission(@PathVariable final int id) {
    try {
      appPermissionService.hardDeleteAppPermission(id);
      return entityDtoConvertUtils.getResponseDeleteAppPermission();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }

  @Operation(
      summary = "Restore a soft-deleted permission by ID",
      description =
          "Restores a permission that was previously soft-deleted, identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the permission to restore", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission restored successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Permission Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppPermissionResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/permission/{id}/restore")
  public ResponseEntity<AppPermissionResponse> restoreAppPermission(@PathVariable final int id) {
    try {
      final AppPermissionEntity appPermissionEntity =
          appPermissionService.restoreSoftDeletedAppPermission(id);
      return entityDtoConvertUtils.getResponseSingleAppPermission(appPermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppPermission(ex);
    }
  }
}
