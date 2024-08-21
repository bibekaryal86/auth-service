package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getBaseUrlForLinkInEmail;
import static user.management.system.app.util.JwtUtils.encodeAuthCredentials;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.connector.AuthenvServiceConnector;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.service.AppUserPasswordService;
import user.management.system.app.service.AppUserService;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.service.AppsService;
import user.management.system.app.service.EmailService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Tag(name = "App User Controller", description = "View and Manage App Users")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/basic_app_users/user")
public class AppUserBasicAuthController {

  private final AppUserService appUserService;
  private final AppsService appsService;
  private final AppsAppUserService appsAppUserService;
  private final AppUserPasswordService appUserPasswordService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EmailService emailService;
  private final AuthenvServiceConnector authenvServiceConnector;

  @PostMapping("/{appId}/create")
  public ResponseEntity<AppUserResponse> createAppUser(
      @PathVariable final String appId,
      @RequestBody final AppUserRequest appUserRequest,
      final HttpServletRequest request) {
    try {
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      final AppsEntity appsEntity = appsService.readApp(appId);
      final AppUserEntity appUserEntity =
          appUserService.createAppUser(appsEntity, appUserRequest, baseUrl);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PostMapping("/{appId}/login")
  public ResponseEntity<UserLoginResponse> loginAppUser(
      @PathVariable final String appId, @RequestBody final UserLoginRequest userLoginRequest) {
    try {
      final AppUserEntity appUserEntity = appUserPasswordService.loginUser(appId, userLoginRequest);
      final AppUserDto appUserDto =
          entityDtoConvertUtils.convertEntityToDtoAppUser(appUserEntity, true);
      final String token = encodeAuthCredentials(appUserDto);
      return ResponseEntity.ok(UserLoginResponse.builder().token(token).user(appUserDto).build());
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserLogin(ex);
    }
  }

  @PostMapping("/{appId}/reset")
  public ResponseEntity<ResponseStatusInfo> resetAppUser(
      @PathVariable final String appId, @RequestBody final UserLoginRequest userLoginRequest) {
    try {
      appUserPasswordService.resetUser(appId, userLoginRequest);
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorValidateReset(ex);
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
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorValidateReset(ex);
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
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorValidateReset(ex);
    }
  }
}
