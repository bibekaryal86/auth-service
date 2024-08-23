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
import user.management.system.app.model.dto.AppRoleRequest;
import user.management.system.app.model.dto.AppRoleResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.service.AppRoleService;
import user.management.system.app.service.AuditService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Roles Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_roles")
@Validated
public class AppRoleController {

  private final AppRoleService appRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuditService auditService;

  @Operation(
      summary = "Create a new role",
      description = "Creates a new role",
      security = @SecurityRequirement(name = "Token"),
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the role to create",
              content = @Content(schema = @Schema(implementation = AppRoleRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
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
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class)))
      })
  @CheckPermission("ROLE_CREATE")
  @PostMapping("/role")
  public ResponseEntity<AppRoleResponse> createAppRole(
      @Valid @RequestBody final AppRoleRequest appRoleRequest, final HttpServletRequest request) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.createAppRole(appRoleRequest);
      auditService.auditAppRoleCreate(request, appRoleEntity);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @Operation(
      summary = "Get all roles",
      description = "Retrieves all roles available in the system",
      security = @SecurityRequirement(name = "Token"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class)))
      })
  @CheckPermission("ROLE_READ")
  @GetMapping
  public ResponseEntity<AppRoleResponse> readAppRoles() {
    try {
      final List<AppRoleEntity> appRoleEntities = appRoleService.readAppRoles();
      return entityDtoConvertUtils.getResponseMultipleAppRole(appRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @Operation(
      summary = "Get role by ID",
      description = "Retrieves a specific role by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the role to retrieve", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Role Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class)))
      })
  @CheckPermission("ROLE_READ")
  @GetMapping("/role/{id}")
  public ResponseEntity<AppRoleResponse> readAppRole(@PathVariable final int id) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.readAppRole(id);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @Operation(
      summary = "Update a role by ID",
      description = "Updates an existing role identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the role to update", required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the role to update",
              required = true,
              content = @Content(schema = @Schema(implementation = AppRoleRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
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
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Role Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class)))
      })
  @CheckPermission("ROLE_UPDATE")
  @PutMapping("/role/{id}")
  public ResponseEntity<AppRoleResponse> updateAppRole(
      @PathVariable final int id,
      @Valid @RequestBody final AppRoleRequest appRoleRequest,
      final HttpServletRequest request) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.updateAppRole(id, appRoleRequest);
      auditService.auditAppRoleUpdate(request, appRoleEntity);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @Operation(
      summary = "Soft delete a role by ID",
      description =
          "Marks a role as deleted without permanently removing it, identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the role to soft delete", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role soft deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Role Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class)))
      })
  @CheckPermission("ROLE_DELETE")
  @DeleteMapping("/role/{id}")
  public ResponseEntity<AppRoleResponse> softDeleteAppRole(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      appRoleService.softDeleteAppRole(id);
      auditService.auditAppRoleDeleteSoft(request, id);
      return entityDtoConvertUtils.getResponseDeleteAppRole();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @Operation(
      summary = "Hard delete a role by ID",
      description = "Permanently removes a role identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the role to hard delete", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role hard deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Role Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/role/{id}/hard")
  public ResponseEntity<AppRoleResponse> hardDeleteAppRole(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      appRoleService.hardDeleteAppRole(id);
      auditService.auditAppRoleDeleteHard(request, id);
      return entityDtoConvertUtils.getResponseDeleteAppRole();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }

  @Operation(
      summary = "Restore a soft-deleted role by ID",
      description = "Restores a role that was previously soft-deleted, identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the role to restore", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role restored successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Role Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRoleResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/role/{id}/restore")
  public ResponseEntity<AppRoleResponse> restoreAppRole(
      @PathVariable final int id, final HttpServletRequest request) {
    try {
      final AppRoleEntity appRoleEntity = appRoleService.restoreSoftDeletedAppRole(id);
      auditService.auditAppRoleRestore(request, id);
      return entityDtoConvertUtils.getResponseSingleAppRole(appRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRole(ex);
    }
  }
}
