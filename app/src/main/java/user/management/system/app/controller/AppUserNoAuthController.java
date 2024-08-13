package user.management.system.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.service.AppUserService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RestController
@RequestMapping("/na_app_users")
public class AppUserNoAuthController {

  private final AppUserService appUserService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  public AppUserNoAuthController(final AppUserService appUserService, final EntityDtoConvertUtils entityDtoConvertUtils) {
    this.appUserService = appUserService;
    this.entityDtoConvertUtils = entityDtoConvertUtils;
  }

  @PostMapping
  public ResponseEntity<AppUserResponse> createAppUser(
      @RequestBody final AppUserRequest appUserRequest) {
    try {
      AppUserEntity appUserEntity = appUserService.createAppUser(appUserRequest);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }
}
