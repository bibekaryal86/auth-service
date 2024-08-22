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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppRolePermissionRequest;
import user.management.system.app.model.dto.AppRolePermissionResponse;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.service.AppRolePermissionService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Roles Permissions Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_roles_permissions")
public class AppRolePermissionController {

  private final AppRolePermissionService appRolePermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @Operation(
      summary = "Assign Permission to a Role",
      description = "Adds a Permission to a Role",
      security = @SecurityRequirement(name = "Token"),
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of permission and role to be linked",
              content =
                  @Content(schema = @Schema(implementation = AppRolePermissionRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission assigned successfully to role",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Role or Permission to Link Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class)))
      })
  @CheckPermission("ROLE_PERMISSION_ASSIGN")
  @PostMapping("/role_permission")
  public ResponseEntity<AppRolePermissionResponse> createAppRolePermission(
      @RequestBody final AppRolePermissionRequest appRolePermissionRequest) {
    try {
      final AppRolePermissionEntity appRolePermissionEntity =
          appRolePermissionService.createAppRolePermission(appRolePermissionRequest);
      return entityDtoConvertUtils.getResponseSingleAppRolePermission(appRolePermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @Operation(
      summary = "Get all roles and permissions linked",
      description = "Retrieves all linked roles and permissions available in the system",
      security = @SecurityRequirement(name = "Token"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles Permissions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class)))
      })
  @CheckPermission({"ROLE_READ", "PERMISSION_READ"})
  @GetMapping
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermissions() {
    try {
      final List<AppRolePermissionEntity> appRolePermissionEntities =
          appRolePermissionService.readAppRolePermissions();
      return entityDtoConvertUtils.getResponseMultipleAppRolePermission(appRolePermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @Operation(
      summary = "Get linked roles and permissions by role ID",
      description = "Retrieves all roles and permissions associated with the specified role ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "roleId",
            description = "ID of the role for which roles permissions are retrieved",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles Permissions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class)))
      })
  @CheckPermission({"ROLE_READ", "PERMISSION_READ"})
  @GetMapping("/role/{roleId}")
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermissionsByRoleId(
      @PathVariable final int roleId) {
    try {
      final List<AppRolePermissionEntity> appRolePermissionEntities =
          appRolePermissionService.readAppRolePermissions(roleId);
      return entityDtoConvertUtils.getResponseMultipleAppRolePermission(appRolePermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @Operation(
      summary = "Get linked roles and permissions by role ID and app ID",
      description =
          "Retrieves all roles and permissions associated with the specified role ID for a given application ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which roles permissions are retrieved",
            required = true),
        @Parameter(
            name = "roleIds",
            description = "IDs of the roles for which roles permissions are retrieved",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles Permissions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class)))
      })
  @CheckPermission({"ROLE_READ", "PERMISSION_READ"})
  @GetMapping("/app/{appId}/roles/{roleIds}")
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermissionsByRoleIds(
      @PathVariable final String appId, @PathVariable final List<Integer> roleIds) {
    try {
      final List<AppRolePermissionEntity> appRolePermissionEntities =
          appRolePermissionService.readAppRolePermissions(appId, roleIds);
      return entityDtoConvertUtils.getResponseMultipleAppRolePermission(appRolePermissionEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @Operation(
      summary = "Get a linked role permission role by role ID and permission ID",
      description = "Retrieves a specific role permission by its role ID and permission ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "roleId",
            description = "ID of the role in linked role permission",
            required = true),
        @Parameter(
            name = "permissionId",
            description = "ID of the permission in linked role permission",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role Permission retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Role Permission Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class)))
      })
  @CheckPermission({"ROLE_READ", "PERMISSION_READ"})
  @GetMapping("/role_permission/{roleId}/{permissionId}")
  public ResponseEntity<AppRolePermissionResponse> readAppRolePermission(
      @PathVariable final int roleId, @PathVariable final int permissionId) {
    try {
      final AppRolePermissionEntity appRolePermissionEntity =
          appRolePermissionService.readAppRolePermission(roleId, permissionId);
      return entityDtoConvertUtils.getResponseSingleAppRolePermission(appRolePermissionEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }

  @Operation(
      summary = "Unassign Permission from a Role",
      description = "Removes a Permission from a Role",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "roleId",
            description = "ID of the role in linked role permission",
            required = true),
        @Parameter(
            name = "permissionId",
            description = "ID of the permission in linked role permission",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission unassigned successfully from role",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Role Permission Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppRolePermissionResponse.class)))
      })
  @CheckPermission("ROLE_PERMISSION_UNASSIGN")
  @DeleteMapping("/role_permission/{roleId}/{permissionId}")
  public ResponseEntity<AppRolePermissionResponse> deleteAppRolePermission(
      @PathVariable final int roleId, @PathVariable final int permissionId) {
    try {
      appRolePermissionService.deleteAppRolePermission(roleId, permissionId);
      return entityDtoConvertUtils.getResponseDeleteAppRolePermission();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppRolePermission(ex);
    }
  }
}
