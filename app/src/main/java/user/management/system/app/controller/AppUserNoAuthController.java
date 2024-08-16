package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getBaseUrlForLinkInEmail;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.service.AppUserPasswordService;
import user.management.system.app.service.AppUserService;
import user.management.system.app.util.EntityDtoConvertUtils;
import user.management.system.app.util.JwtUtils;

@RestController
@RequestMapping("/api/v1/na_app_users")
public class AppUserNoAuthController {

  private final AppUserService appUserService;
  private final AppUserPasswordService appUserPasswordService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  public AppUserNoAuthController(
      final AppUserService appUserService,
      final AppUserPasswordService appUserPasswordService,
      final EntityDtoConvertUtils entityDtoConvertUtils) {
    this.appUserService = appUserService;
    this.appUserPasswordService = appUserPasswordService;
    this.entityDtoConvertUtils = entityDtoConvertUtils;
  }

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
      final String token = JwtUtils.encodeAuthCredentials(appUserDto);
      return ResponseEntity.ok(UserLoginResponse.builder().token(token).user(appUserDto).build());
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserLogin(ex);
    }
  }
}
