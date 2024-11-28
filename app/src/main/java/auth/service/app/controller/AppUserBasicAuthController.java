package auth.service.app.controller;

import static auth.service.app.util.CommonUtils.getBaseUrlForLinkInEmail;
import static auth.service.app.util.JwtUtils.decodeAuthCredentials;
import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.model.dto.AppTokenRequest;
import auth.service.app.model.dto.AppUserRequest;
import auth.service.app.model.dto.AppUserResponse;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.model.dto.UserLoginRequest;
import auth.service.app.model.dto.UserLoginResponse;
import auth.service.app.model.entity.AppTokenEntity;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.service.AppTokenService;
import auth.service.app.service.AppUserPasswordService;
import auth.service.app.service.AppUserService;
import auth.service.app.service.AppsAppUserService;
import auth.service.app.service.AppsService;
import auth.service.app.service.AuditService;
import auth.service.app.service.EmailService;
import auth.service.app.util.EntityDtoConvertUtils;
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
      runAsync(
          () ->
              auditService.auditAppUserCreate(
                  request, appId, appUserEntity, appUserRequest.isGuestUser()));
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      log.error("Create App User: [{}] | [{}]", appId, appUserRequest, ex);
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PostMapping("/{appId}/login")
  public ResponseEntity<UserLoginResponse> loginAppUser(
      @PathVariable final String appId,
      @Valid @RequestBody final UserLoginRequest userLoginRequest,
      final HttpServletRequest request) {
    try {
      final UserLoginResponse userLoginResponse =
          appUserPasswordService.loginUser(appId, userLoginRequest);
      runAsync(
          () ->
              auditService.auditAppUserLoginSuccess(
                  request, appId, userLoginResponse.getUser().getId()));
      return ResponseEntity.ok(userLoginResponse);
    } catch (Exception ex) {
      log.error("Login App User: [{}] | [{}]", appId, userLoginRequest, ex);
      runAsync(
          () ->
              auditService.auditAppUserLoginFailure(
                  request, appId, userLoginRequest.getEmail(), ex));
      return entityDtoConvertUtils.getResponseErrorAppUserLogin(ex);
    }
  }

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
      runAsync(
          () ->
              auditService.auditAppUserTokenRefreshSuccess(
                  request, appId, appTokenEntity.getUser()));
      return ResponseEntity.ok(userLoginResponse);
    } catch (Exception ex) {
      log.error("Refresh Token: [{}] | [{}]", appId, appTokenRequest, ex);
      runAsync(
          () ->
              auditService.auditAppUserTokenRefreshFailure(
                  request, appId, appTokenRequest.getAppUserId(), ex));
      return entityDtoConvertUtils.getResponseErrorAppUserLogin(ex);
    }
  }

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

      runAsync(
          () -> auditService.auditAppUserLogoutSuccess(request, appId, appTokenEntity.getUser()));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Logout: [{}] | [{}]", appId, appTokenRequest, ex);
      runAsync(
          () ->
              auditService.auditAppUserLogoutFailure(
                  request, appId, appTokenRequest.getAppUserId(), ex));
      return entityDtoConvertUtils.getResponseErrorResponseStatusInfo(ex);
    }
  }

  @PostMapping("/{appId}/reset")
  public ResponseEntity<ResponseStatusInfo> resetAppUser(
      @PathVariable final String appId,
      @Valid @RequestBody final UserLoginRequest userLoginRequest,
      final HttpServletRequest request) {
    try {
      final AppUserEntity appUserEntity = appUserPasswordService.resetUser(appId, userLoginRequest);
      runAsync(() -> auditService.auditAppUserResetSuccess(request, appId, appUserEntity));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Reset App User: [{}] | [{}]", appId, userLoginRequest, ex);
      runAsync(
          () ->
              auditService.auditAppUserResetFailure(
                  request, appId, userLoginRequest.getEmail(), ex));
      return entityDtoConvertUtils.getResponseErrorResponseStatusInfo(ex);
    }
  }

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
      runAsync(
          () ->
              auditService.auditAppUserValidateInit(
                  request, appId, appsAppUserEntity.getAppUser()));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Validate App User Init: [{}], [{}]", appId, email, ex);
      runAsync(() -> auditService.auditAppUserValidateFailure(request, appId, email, ex));
      return entityDtoConvertUtils.getResponseErrorResponseStatusInfo(ex);
    }
  }

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
      runAsync(
          () -> auditService.auditAppUserResetInit(request, appId, appsAppUserEntity.getAppUser()));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Reset App User Init: [{}], [{}]", appId, email, ex);
      runAsync(() -> auditService.auditAppUserResetFailure(request, appId, email, ex));
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
