package user.management.system.app.controller;

import static user.management.system.app.util.CommonUtils.getBaseUrlForLinkInEmail;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppRolePermissionEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.service.AppRolePermissionService;
import user.management.system.app.service.AppUserPasswordService;
import user.management.system.app.service.AppUserRoleService;
import user.management.system.app.service.AppUserService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RestController
@RequestMapping("/api/v1/na_app_users")
public class AppUserNoAuthController {

  private final AppUserService appUserService;
  private final AppUserPasswordService appUserPasswordService;
  private final AppUserRoleService appUserRoleService;
  private final AppRolePermissionService appRolePermissionService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  public AppUserNoAuthController(
      final AppUserService appUserService,
      final AppUserPasswordService appUserPasswordService,
      final AppUserRoleService appUserRoleService,
      final AppRolePermissionService appRolePermissionService,
      final EntityDtoConvertUtils entityDtoConvertUtils) {
    this.appUserService = appUserService;
    this.appUserPasswordService = appUserPasswordService;
    this.appUserRoleService = appUserRoleService;
    this.appRolePermissionService = appRolePermissionService;
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
  public ResponseEntity<UserLoginResponse> loginAppUser(@RequestBody final UserLoginRequest userLoginRequest) {
    try {
      AppUserEntity appUserEntity = appUserPasswordService.loginUser(userLoginRequest);
      List<AppUserRoleEntity> appUserRoleEntities = appUserRoleService.readAppUserRoles(appUserEntity.getId());
      List<AppRoleEntity> appRoleEntities = appUserRoleEntities.stream().map(AppUserRoleEntity::getAppRole).toList();
      List<Integer> appRoleIds = appRoleEntities.stream().map(AppRoleEntity::getId).toList();
      List<AppRolePermissionEntity> appRolePermissionEntities = appRolePermissionService.readAppRolePermissions(appRoleIds);
      List<AppPermissionEntity> appPermissionEntities = appRolePermissionEntities.stream().map(AppRolePermissionEntity::getAppPermission).toList();





      AppUserDto appUserDto = entityDtoConvertUtils.convertEntityToDtoAppUser(appUserEntity);


      return null;
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUserLogin(ex);
    }
  }
}
