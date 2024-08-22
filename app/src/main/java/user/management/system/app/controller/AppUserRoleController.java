package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppUserRoleRequest;
import user.management.system.app.model.dto.AppUserRoleResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.service.AppUserRoleService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Users Roles Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_users_roles")
@Validated
public class AppUserRoleController {

  private final AppUserRoleService appUserRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @Operation(
      summary = "Assign Role to a User",
      description = "Adds a Role to a User",
      security = @SecurityRequirement(name = "Token"),
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of user and role to be linked",
              content = @Content(schema = @Schema(implementation = AppUserRoleRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role assigned successfully to user",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
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
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User or Role to Link Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class)))
      })
  @CheckPermission("USER_ROLE_ASSIGN")
  @PostMapping("/user_role")
  public ResponseEntity<AppUserRoleResponse> createAppUserRole(
      @Valid @RequestBody final AppUserRoleRequest appUserRoleRequest) {
    try {
      final AppUserRoleEntity appUserRoleEntity =
          appUserRoleService.createAppUserRole(appUserRoleRequest);
      return entityDtoConvertUtils.getResponseSingleAppUserRole(appUserRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @Operation(
      summary = "Get all users and roles linked",
      description = "Retrieves all linked users and roles available in the system",
      security = @SecurityRequirement(name = "Token"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Users Roles retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class)))
      })
  @CheckPermission({"USER_READ", "ROLE_READ"})
  @GetMapping
  public ResponseEntity<AppUserRoleResponse> readAppUserRoles() {
    try {
      final List<AppUserRoleEntity> appUserRoleEntities = appUserRoleService.readAppUserRoles();
      return entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @Operation(
      summary = "Get linked users and roles by user ID",
      description = "Retrieves all users and roles associated with the specified user ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "userId",
            description = "ID of the user for which users roles are retrieved",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Users Roles retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class)))
      })
  @CheckPermission({"USER_READ", "ROLE_READ"})
  @GetMapping("/user/{userId}")
  public ResponseEntity<AppUserRoleResponse> readAppUserRolesByUserId(
      @PathVariable final int userId) {
    try {
      final List<AppUserRoleEntity> appUserRoleEntities =
          appUserRoleService.readAppUserRoles(userId);
      return entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @Operation(
      summary = "Get linked users and roles by a list of user IDs",
      description = "Retrieves all users and roles associated with the specified user IDs",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "userIds",
            description = "IDs of the users for which users roles are retrieved",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Apps Users retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class)))
      })
  @CheckPermission({"USER_READ", "ROLE_READ"})
  @GetMapping("/users/{userIds}")
  public ResponseEntity<AppUserRoleResponse> readAppUserRolesByUserIds(
      @PathVariable final List<Integer> userIds) {
    try {
      final List<AppUserRoleEntity> appUserRoleEntities =
          appUserRoleService.readAppUserRoles(userIds);
      return entityDtoConvertUtils.getResponseMultipleAppUserRole(appUserRoleEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @Operation(
      summary = "Get a linked user role by user ID and role ID",
      description = "Retrieves a specific user role by its user ID and role ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "userId",
            description = "ID of the user in linked user role",
            required = true),
        @Parameter(
            name = "roleId",
            description = "ID of the role in linked user role",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User Role retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User Role Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class)))
      })
  @CheckPermission({"USER_READ", "ROLE_READ"})
  @GetMapping("/user_role/{userId}/{roleId}")
  public ResponseEntity<AppUserRoleResponse> readAppUserRole(
      @PathVariable final int userId, @PathVariable final int roleId) {
    try {
      final AppUserRoleEntity appUserRoleEntity =
          appUserRoleService.readAppUserRole(userId, roleId);
      return entityDtoConvertUtils.getResponseSingleAppUserRole(appUserRoleEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }

  @Operation(
      summary = "Unassign Role from User",
      description = "Removes a Role from User",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "userId",
            description = "ID of the User in linked user role",
            required = true),
        @Parameter(
            name = "roleId",
            description = "ID of the Role in linked user role",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Role unassigned successfully from user",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User Role Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserRoleResponse.class)))
      })
  @CheckPermission("USER_ROLE_UNASSIGN")
  @DeleteMapping("/user_role/{userId}/{roleId}")
  public ResponseEntity<AppUserRoleResponse> deleteAppUserRole(
      @PathVariable final int userId, @PathVariable final int roleId) {
    try {
      appUserRoleService.deleteAppUserRole(userId, roleId);
      return entityDtoConvertUtils.getResponseDeleteAppUserRole();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserRole(ex);
    }
  }
}
