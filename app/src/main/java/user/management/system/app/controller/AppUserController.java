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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.service.AppUserService;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.util.EntityDtoConvertUtils;
import user.management.system.app.util.PermissionCheck;

@Tag(name = "Users Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app_users")
@Validated
public class AppUserController {

  private final AppUserService appUserService;
  private final AppsAppUserService appsAppUserService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final PermissionCheck permissionCheck;

  @Operation(
      summary = "Retrieve all app users",
      description = "Fetches a list of all app users available in the system",
      security = @SecurityRequirement(name = "Token"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of app users",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @GetMapping
  public ResponseEntity<AppUserResponse> readAppUsers() {
    try {
      final List<AppUserEntity> appUserEntities = appUserService.readAppUsers();
      final List<AppUserEntity> filteredAppUserEntities =
          permissionCheck.filterAppUserListByAccess(appUserEntities);
      return entityDtoConvertUtils.getResponseMultipleAppUser(filteredAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Retrieve app users by application ID",
      description = "Fetches a list of app users associated with a specific application ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "appId", description = "ID of the application", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of app users associated with the specified application ID",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @GetMapping("/{appId}")
  public ResponseEntity<AppUserResponse> readAppUsers(@PathVariable final String appId) {
    try {
      final List<AppsAppUserEntity> appsAppUserEntities =
          appsAppUserService.readAppsAppUsers(appId);
      final List<AppUserEntity> appUserEntities =
          appsAppUserEntities.stream().map(AppsAppUserEntity::getAppUser).toList();
      final List<AppUserEntity> filteredAppUserEntities =
          permissionCheck.filterAppUserListByAccess(appUserEntities);
      return entityDtoConvertUtils.getResponseMultipleAppUser(filteredAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Retrieve app user by user ID",
      description = "Fetches details of a specific app user by user ID.",
      security = @SecurityRequirement(name = "Token"),
      parameters = {@Parameter(name = "id", description = "ID of the app user", required = true)},
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Details of the specified app user",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User Not Found by specified ID",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @GetMapping("/user/{id}")
  public ResponseEntity<AppUserResponse> readAppUser(@PathVariable final int id) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      final AppUserEntity appUserEntity = appUserService.readAppUser(id);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Retrieve app user by email",
      description = "Fetches details of a specific app user by email.",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "email", description = "Email of the app user", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Details of the specified app user",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User Not Found by specified Email",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @GetMapping("/user/email/{email}")
  public ResponseEntity<AppUserResponse> readAppUser(@PathVariable final String email) {
    try {
      permissionCheck.canUserAccessAppUser(email, 0);
      final AppUserEntity appUserEntity = appUserService.readAppUser(email);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Update app user details",
      description = "Updates details of a specific app user by ID.",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app user to update", required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Updated details of the app user",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = AppUserRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Updated details of the app user",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Required Element Missing",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User to update Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @PutMapping("/user/{id}")
  public ResponseEntity<AppUserResponse> updateAppUser(
      @PathVariable final int id, @Valid @RequestBody final AppUserRequest appUserRequest) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      final AppUserEntity appUserEntity = appUserService.updateAppUser(id, appUserRequest);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Update app user password",
      description = "Updates the password for a specific app user by ID.",
      security = @SecurityRequirement(name = "Token"),
      parameters = {@Parameter(name = "id", description = "ID of the app user", required = true)},
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "New password details for the app user",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = UserLoginRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Updated app user details with new password",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User to update password Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @PutMapping("/user/{id}/password")
  public ResponseEntity<AppUserResponse> updateAppUserPassword(
      @PathVariable final int id, @Valid @RequestBody final UserLoginRequest userLoginRequest) {
    try {
      permissionCheck.canUserAccessAppUser("", id);
      final AppUserEntity appUserEntity =
          appUserService.updateAppUserPassword(id, userLoginRequest);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Soft delete an app user",
      description =
          "Marks a permission as deleted without permanently removing it, identified by its unique ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app user to soft delete", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User soft deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User to soft delete Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN SOFT DELETE USER")
  @DeleteMapping("/user/{id}")
  public ResponseEntity<AppUserResponse> softDeleteAppUser(@PathVariable final int id) {
    try {
      appUserService.softDeleteAppUser(id);
      return entityDtoConvertUtils.getResponseDeleteAppUser();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Hard delete an app user",
      description = "Permanently removes a specific app user by ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app user to hard delete", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User hard deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User to delete Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN HARD DELETE")
  @DeleteMapping("/user/{id}/hard")
  public ResponseEntity<AppUserResponse> hardDeleteAppUser(@PathVariable final int id) {
    try {
      appUserService.hardDeleteAppUser(id);
      return entityDtoConvertUtils.getResponseDeleteAppUser();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Restore a soft-deleted app user",
      description = "Restores a soft-deleted app user by ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(name = "id", description = "ID of the app user to restore", required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User restored successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authenticated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Not Authorized or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User to restore Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN RESTORE")
  @PatchMapping("/user/{id}/restore")
  public ResponseEntity<AppUserResponse> restoreAppUser(@PathVariable final int id) {
    try {
      final AppUserEntity appUserEntity = appUserService.restoreSoftDeletedAppUser(id);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }
}
