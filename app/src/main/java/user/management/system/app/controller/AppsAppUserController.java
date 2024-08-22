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
import user.management.system.app.model.dto.AppsAppUserRequest;
import user.management.system.app.model.dto.AppsAppUserResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Apps Users Management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/apps_app_user")
@Validated
public class AppsAppUserController {

  private final AppsAppUserService appsAppUserService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  @Operation(
      summary = "Assign User to an App",
      description = "Adds a User to an App",
      security = @SecurityRequirement(name = "Token"),
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of user and app to be linked",
              content = @Content(schema = @Schema(implementation = AppsAppUserRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User assigned successfully to app",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
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
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User or App to Link Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN ASSIGN USER TO APPS")
  @PostMapping("/apps_user")
  public ResponseEntity<AppsAppUserResponse> createAppAppsAppUser(
      @Valid @RequestBody final AppsAppUserRequest appAppsAppUserRequest) {
    try {
      final AppsAppUserEntity appAppsAppUserEntity =
          appsAppUserService.createAppsAppUser(appAppsAppUserRequest);
      return entityDtoConvertUtils.getResponseSingleAppsAppUser(appAppsAppUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @Operation(
      summary = "Get all apps and users linked",
      description = "Retrieves all linked apps and users available in the system",
      security = @SecurityRequirement(name = "Token"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Apps Users retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping
  public ResponseEntity<AppsAppUserResponse> readAppAppsAppUsers() {
    try {
      final List<AppsAppUserEntity> appAppsAppUserEntities = appsAppUserService.readAppsAppUsers();
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appAppsAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @Operation(
      summary = "Get linked apps and users by app ID",
      description = "Retrieves all apps and users associated with the specified app ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the app for which apps users are retrieved",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Apps Users retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping("/app/{appId}")
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsersByAppId(
      @PathVariable final String appId) {
    try {
      final List<AppsAppUserEntity> appAppsAppUserEntities =
          appsAppUserService.readAppsAppUsers(appId);
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appAppsAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @Operation(
      summary = "Get linked apps and users by user ID",
      description = "Retrieves all apps and users associated with the specified user ID",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "appUserId",
            description = "ID of the user for which apps users are retrieved",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Apps Users retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping("/user/{appUserId}")
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsersByUserId(
      @PathVariable final int appUserId) {
    try {
      final List<AppsAppUserEntity> appAppsAppUserEntities =
          appsAppUserService.readAppsAppUsers(appUserId);
      return entityDtoConvertUtils.getResponseMultipleAppsAppUser(appAppsAppUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @Operation(
      summary = "Get a linked app user by app ID and user email",
      description = "Retrieves a specific app user its app ID and user email",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the app in linked app user",
            required = true),
        @Parameter(
            name = "appUserEmail",
            description = "Email of the User in linked app user",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "App User retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - App User Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN READ APPS AND USERS")
  @GetMapping("/app/{appId}/user/{appUserEmail}")
  public ResponseEntity<AppsAppUserResponse> readAppsAppUsersByAppIdAndUserEmail(
      @PathVariable final String appId, @PathVariable final String appUserEmail) {
    try {
      final AppsAppUserEntity appsAppUserEntity =
          appsAppUserService.readAppsAppUser(appId, appUserEmail);
      return entityDtoConvertUtils.getResponseSingleAppsAppUser(appsAppUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }

  @Operation(
      summary = "Unassign User from an App",
      description = "Removes a User from an App",
      security = @SecurityRequirement(name = "Token"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the Application in linked app user",
            required = true),
        @Parameter(
            name = "appUserEmail",
            description = "Email of the User in linked app user",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User unassigned successfully from app",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - User Not Authorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User Forbidden or Not Validated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User App Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppsAppUserResponse.class)))
      })
  @CheckPermission("ONLY SUPERUSER CAN UNASSIGN USER FROM APP")
  @DeleteMapping("/apps_user/{appId}/{appUserEmail}")
  public ResponseEntity<AppsAppUserResponse> deleteAppsAppUser(
      @PathVariable final String appId, @PathVariable final String appUserEmail) {
    try {
      appsAppUserService.deleteAppsAppUser(appId, appUserEmail);
      return entityDtoConvertUtils.getResponseDeleteAppsAppUser();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppsAppUser(ex);
    }
  }
}
