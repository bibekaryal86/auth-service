package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getBaseUrlForLinkInEmail;
import static user.management.system.app.util.JwtUtils.encodeAuthCredentials;

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
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.ResponseStatusInfo;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.service.AppUserPasswordService;
import user.management.system.app.service.AppUserService;
import user.management.system.app.service.EmailService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/na_app_users/user")
public class AppUserNoAuthController {

  private final AppUserService appUserService;
  private final AppUserPasswordService appUserPasswordService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EmailService emailService;

  @PostMapping("/create")
  public ResponseEntity<AppUserResponse> createAppUser(
      @RequestBody final AppUserRequest appUserRequest, final HttpServletRequest request) {
    try {
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      final AppUserEntity appUserEntity = appUserService.createAppUser(appUserRequest, baseUrl);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponse> loginAppUser(
      @RequestBody final UserLoginRequest userLoginRequest) {
    try {
      final AppUserEntity appUserEntity = appUserPasswordService.loginUser(userLoginRequest);
      final AppUserDto appUserDto =
          entityDtoConvertUtils.convertEntityToDtoAppUserWithRolesPermissions(appUserEntity);
      final String token = encodeAuthCredentials(appUserDto);
      return ResponseEntity.ok(UserLoginResponse.builder().token(token).user(appUserDto).build());
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserLogin(ex);
    }
  }

  @PostMapping("/reset")
  public ResponseEntity<ResponseStatusInfo> resetAppUser(
      @RequestBody final UserLoginRequest userLoginRequest) {
    try {
      appUserPasswordService.resetUser(userLoginRequest);
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorValidateReset(ex);
    }
  }

  @GetMapping("/app/{app}/validate_init")
  public ResponseEntity<ResponseStatusInfo> validateAppUserInit(
      @PathVariable final String app,
      @RequestParam final String email,
      final HttpServletRequest request) {
    try {
      final AppUserEntity appUserEntity = appUserService.readAppUser(app, email);
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      emailService.sendUserValidationEmail(appUserEntity, baseUrl);
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorValidateReset(ex);
    }
  }

  @GetMapping("/app/{app}/validate_exit")
  public ResponseEntity<Void> validateAppUserExit(
      @PathVariable final String app,
      @RequestParam final String toValidate,
      @RequestParam final String redirectUrl) {
    try {
      appUserPasswordService.validateAndResetUser(app, toValidate, true);
      return entityDtoConvertUtils.getResponseValidateUser(redirectUrl, true);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseValidateUser(redirectUrl, false);
    }
  }

  @GetMapping("/app/{app}/reset_init")
  public ResponseEntity<ResponseStatusInfo> resetAppUserInit(
      @PathVariable final String app,
      @RequestParam final String email,
      final HttpServletRequest request) {
    try {
      final AppUserEntity appUserEntity = appUserService.readAppUser(app, email);
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      emailService.sendUserResetEmail(appUserEntity, baseUrl);
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorValidateReset(ex);
    }
  }

  @GetMapping("/app/{app}/reset_exit")
  public ResponseEntity<Void> resetAppUserMid(
      @PathVariable final String app,
      @RequestParam final String toReset,
      @RequestParam final String redirectUrl) {
    try {
      final String userToReset = appUserPasswordService.validateAndResetUser(app, toReset, false);
      return entityDtoConvertUtils.getResponseResetUser(redirectUrl, true, userToReset);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseResetUser(redirectUrl, true, "");
    }
  }
}
