package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getBaseUrlForLinkInEmail;
import static user.management.system.app.util.JwtUtils.decodeAuthCredentials;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.exception.ElementMissingException;
import user.management.system.app.exception.JwtInvalidException;
import user.management.system.app.model.dto.AppTokenRequest;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppTokenEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.model.token.AuthToken;
import user.management.system.app.service.AppTokenService;
import user.management.system.app.service.AppUserPasswordService;
import user.management.system.app.service.AppUserService;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.service.AppsService;
import user.management.system.app.service.AuditService;
import user.management.system.app.service.EmailService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "Users Management")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/basic_app_users/user")
@Validated
public class AppUserBasicAuthController {

  private final AppUserService appUserService;
  private final AppsService appsService;
  private final AppsAppUserService appsAppUserService;
  private final AppUserPasswordService appUserPasswordService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EmailService emailService;
  private final AppTokenService appTokenService;
  private final AuditService auditService;

  @Operation(
      summary = "Create a new user for an application",
      description = "Creates a new user for the specified application ID",
      security = @SecurityRequirement(name = "Basic"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which the user is created",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the user to create",
              required = true,
              content = @Content(schema = @Schema(implementation = AppUserRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid or missing data",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing/Incorrect credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppUserResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Application Not Found",
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
  @PostMapping("/{appId}/create")
  public ResponseEntity<AppUserResponse> createAppUser(
      @PathVariable final String appId,
      @Valid @RequestBody final AppUserRequest appUserRequest,
      final HttpServletRequest request) {
    try {
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      final AppsEntity appsEntity = appsService.readApp(appId);
      final AppUserEntity appUserEntity =
          appUserService.createAppUser(appsEntity, appUserRequest, baseUrl);
      auditService.auditAppUserCreate(request, appId, appUserEntity, appUserRequest.isGuestUser());
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      log.error("Create App User: [{}] | [{}]", appId, appUserRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @Operation(
      summary = "Login a user for an application",
      description =
          "Authenticates a user for the specified application ID and returns access and refresh tokens",
      security = @SecurityRequirement(name = "Basic"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which the user is logging in",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "User login details",
              required = true,
              content = @Content(schema = @Schema(implementation = UserLoginRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Required Element Missing",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing/Incorrect credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User Not Found for App",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)))
      })
  @PostMapping("/{appId}/login")
  public ResponseEntity<UserLoginResponse> loginAppUser(
      @PathVariable final String appId,
      @Valid @RequestBody final UserLoginRequest userLoginRequest,
      final HttpServletRequest request) {
    try {
      final UserLoginResponse userLoginResponse =
          appUserPasswordService.loginUser(appId, userLoginRequest);
      auditService.auditAppUserLoginSuccess(request, appId, userLoginResponse.getUser().getId());
      return ResponseEntity.ok(userLoginResponse);
    } catch (Exception ex) {
      log.error("Login App User: [{}] | [{}]", appId, userLoginRequest, ex);
      auditService.auditAppUserLoginFailure(request, appId, userLoginRequest.getEmail(), ex);
      return entityDtoConvertUtils.getResponseErrorAppUserLogin(ex);
    }
  }

  @Operation(
      summary = "Refresh authentication token",
      description =
          "Refreshes auth token for the specified application ID and returns access and refresh tokens",
      security = @SecurityRequirement(name = "Basic"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which the token is being refreshed",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Token request details",
              required = true,
              content = @Content(schema = @Schema(implementation = AppTokenRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Refresh successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing/Incorrect credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid Credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User Token not found for input refresh token",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)))
      })
  @PostMapping("/{appId}/refresh")
  public ResponseEntity<UserLoginResponse> refreshToken(
      @PathVariable final String appId,
      @Valid @RequestBody final AppTokenRequest appTokenRequest,
      final HttpServletRequest request) {
    try {
      if (!StringUtils.hasText(appTokenRequest.getRefreshToken())) {
        throw new ElementMissingException("Token", "Refresh");
      }

      Map<String, AuthToken> emailAuthToken =
          decodeAuthCredentials(appTokenRequest.getRefreshToken());
      final AppTokenEntity appTokenEntity =
          appTokenService.readTokenByRefreshToken(appTokenRequest.getRefreshToken());

      checkValidToken(appId, emailAuthToken, appTokenEntity);

      final UserLoginResponse userLoginResponse =
          appTokenService.saveToken(appTokenEntity.getId(), null, appTokenEntity.getUser(), appId);
      auditService.auditAppUserTokenRefreshSuccess(request, appId, appTokenEntity.getUser());
      return ResponseEntity.ok(userLoginResponse);
    } catch (Exception ex) {
      log.error("Refresh Token: [{}] | [{}]", appId, appTokenRequest, ex);
      auditService.auditAppUserTokenRefreshFailure(
          request, appId, appTokenRequest.getAppUserId(), ex);
      return entityDtoConvertUtils.getResponseErrorAppUserLogin(ex);
    }
  }

  @Operation(
      summary = "Logout user from an application",
      description = "Logs out user by removing tokens from the system",
      security = @SecurityRequirement(name = "Basic"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application from which user is logging out",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Logout request details",
              required = true,
              content = @Content(schema = @Schema(implementation = AppTokenRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing/Incorrect credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid Credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - User Token not found for input access token",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)))
      })
  @PostMapping("/{appId}/logout")
  public ResponseEntity<ResponseStatusInfo> logout(
      @PathVariable final String appId,
      @Valid @RequestBody final AppTokenRequest appTokenRequest,
      final HttpServletRequest request) {
    try {
      if (!StringUtils.hasText(appTokenRequest.getAccessToken())) {
        throw new ElementMissingException("Token", "Access");
      }

      Map<String, AuthToken> emailAuthToken =
          decodeAuthCredentials(appTokenRequest.getRefreshToken());
      final AppTokenEntity appTokenEntity =
          appTokenService.readTokenByAccessToken(appTokenRequest.getAccessToken());

      checkValidToken(appId, emailAuthToken, appTokenEntity);

      appTokenService.saveToken(
          appTokenEntity.getId(), LocalDateTime.now(), appTokenEntity.getUser(), appId);

      auditService.auditAppUserLogoutSuccess(request, appId, appTokenEntity.getUser());
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Logout: [{}] | [{}]", appId, appTokenRequest, ex);
      auditService.auditAppUserLogoutFailure(request, appId, appTokenRequest.getAppUserId(), ex);
      return entityDtoConvertUtils.getResponseErrorResponseStatusInfo(ex);
    }
  }

  @Operation(
      summary = "Reset user password",
      description = "Resets the password for a user associated with the specified application ID",
      security = @SecurityRequirement(name = "Basic"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which the user's password is being reset",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Details of the user for whom the password will be reset",
              required = true,
              content = @Content(schema = @Schema(implementation = UserLoginRequest.class))),
      responses = {
        @ApiResponse(responseCode = "204", description = "Password reset successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing/Incorrect Credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid Credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Application or User Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class)))
      })
  @PostMapping("/{appId}/reset")
  public ResponseEntity<ResponseStatusInfo> resetAppUser(
      @PathVariable final String appId,
      @Valid @RequestBody final UserLoginRequest userLoginRequest,
      final HttpServletRequest request) {
    try {
      final AppUserEntity appUserEntity = appUserPasswordService.resetUser(appId, userLoginRequest);
      auditService.auditAppUserResetSuccess(request, appId, appUserEntity);
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Reset App User: [{}] | [{}]", appId, userLoginRequest, ex);
      auditService.auditAppUserResetFailure(request, appId, userLoginRequest.getEmail(), ex);
      return entityDtoConvertUtils.getResponseErrorResponseStatusInfo(ex);
    }
  }

  @Operation(
      summary = "Send user validation email",
      description =
          "Sends an email to validate the user associated with the specified application ID",
      security = @SecurityRequirement(name = "Basic"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which the user validation email is sent",
            required = true),
        @Parameter(
            name = "email",
            description = "Email address of the user to validate",
            required = true)
      },
      responses = {
        @ApiResponse(responseCode = "204", description = "Validation email sent successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing/Incorrect Credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid Credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Application or User Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class)))
      })
  @GetMapping("/{appId}/validate_init")
  public ResponseEntity<ResponseStatusInfo> validateAppUserInit(
      @PathVariable final String appId,
      @RequestParam final String email,
      final HttpServletRequest request) {
    try {
      final AppsAppUserEntity appsAppUserEntity = appsAppUserService.readAppsAppUser(appId, email);
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      emailService.sendUserValidationEmail(
          appsAppUserEntity.getApp(), appsAppUserEntity.getAppUser(), baseUrl);
      auditService.auditAppUserValidateInit(request, appId, appsAppUserEntity.getAppUser());
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Validate App User Init: [{}], [{}]", appId, email, ex);
      auditService.auditAppUserValidateFailure(request, appId, email, ex);
      return entityDtoConvertUtils.getResponseErrorResponseStatusInfo(ex);
    }
  }

  @Operation(
      summary = "Send password reset email",
      description =
          "Sends an email to reset the password for the user associated with the specified application ID",
      security = @SecurityRequirement(name = "Basic"),
      parameters = {
        @Parameter(
            name = "appId",
            description = "ID of the application for which the password reset email is sent",
            required = true),
        @Parameter(
            name = "email",
            description = "Email address of the user to reset password",
            required = true)
      },
      responses = {
        @ApiResponse(responseCode = "204", description = "Password reset email sent successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing/Incorrect Credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Invalid Credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Application or User Not Found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Other Errors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseStatusInfo.class)))
      })
  @GetMapping("/{appId}/reset_init")
  public ResponseEntity<ResponseStatusInfo> resetAppUserInit(
      @PathVariable final String appId,
      @RequestParam final String email,
      final HttpServletRequest request) {
    try {
      final AppsAppUserEntity appsAppUserEntity = appsAppUserService.readAppsAppUser(appId, email);
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      emailService.sendUserResetEmail(
          appsAppUserEntity.getApp(), appsAppUserEntity.getAppUser(), baseUrl);
      auditService.auditAppUserResetInit(request, appId, appsAppUserEntity.getAppUser());
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Reset App User Init: [{}], [{}]", appId, email, ex);
      auditService.auditAppUserResetFailure(request, appId, email, ex);
      return entityDtoConvertUtils.getResponseErrorResponseStatusInfo(ex);
    }
  }

  private void checkValidToken(
      final String appId,
      Map<String, AuthToken> emailAuthToken,
      final AppTokenEntity appTokenEntity) {
    Map.Entry<String, AuthToken> firstEntry = emailAuthToken.entrySet().iterator().next();
    String email = firstEntry.getKey();
    AuthToken authToken = firstEntry.getValue();

    if (!Objects.equals(email, appTokenEntity.getUser().getEmail())) {
      throw new JwtInvalidException("Identity Mismatch");
    }

    if (!Objects.equals(appId, authToken.getAppId())) {
      throw new JwtInvalidException("App Mismatch");
    }

    if (appTokenEntity.getDeletedDate() != null) {
      throw new JwtInvalidException("Deleted Token");
    }
  }
}
