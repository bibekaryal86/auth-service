package user.management.system.app.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.dto.AppUserRequest;
import user.management.system.app.model.dto.AppUserResponse;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.service.AppUserService;
import user.management.system.app.util.EntityDtoConvertUtils;

@RestController
@RequestMapping("/app_users")
public class AppUserController {

  private final AppUserService appUserService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  public AppUserController(final AppUserService appUserService, final EntityDtoConvertUtils entityDtoConvertUtils) {
    this.appUserService = appUserService;
    this.entityDtoConvertUtils = entityDtoConvertUtils;
  }

  @GetMapping
  public ResponseEntity<AppUserResponse> readAppUsers() {
    try {
      List<AppUserEntity> appUserEntities = appUserService.readAppUsers();
      return entityDtoConvertUtils.getResponseMultipleAppUser(appUserEntities);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<AppUserResponse> readAppUser(@PathVariable final int id) {
    try {
      AppUserEntity appUserEntity = appUserService.readAppUser(id);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<AppUserResponse> updateAppUser(
      @PathVariable final int id, @RequestBody final AppUserRequest appUserRequest) {
    try {
      AppUserEntity appUserEntity = appUserService.updateAppUser(id, appUserRequest);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<AppUserResponse> softDeleteAppUser(@PathVariable final int id) {
    try {
      appUserService.softDeleteAppUser(id);
      return entityDtoConvertUtils.getResponseDeleteAppUser();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @DeleteMapping("/{id}/hard")
  public ResponseEntity<AppUserResponse> hardDeleteAppUser(@PathVariable final int id) {
    try {
      appUserService.hardDeleteAppUser(id);
      return entityDtoConvertUtils.getResponseDeleteAppUser();
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }

  @PatchMapping("/{id}/restore")
  public ResponseEntity<AppUserResponse> restoreAppUser(@PathVariable final int id) {
    try {
      AppUserEntity appUserEntity = appUserService.restoreSoftDeletedAppUser(id);
      return entityDtoConvertUtils.getResponseSingleAppUser(appUserEntity);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseErrorAppUser(ex);
    }
  }
}
