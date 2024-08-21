package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.connector.AuthenvServiceConnector;
import user.management.system.app.service.AppUserPasswordService;
import user.management.system.app.service.AppUserService;
import user.management.system.app.service.AppsAppUserService;
import user.management.system.app.service.AppsService;
import user.management.system.app.service.EmailService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Hidden
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/na_app_users/user")
public class AppUserNoAuthController {

  private final AppUserService appUserService;
  private final AppsService appsService;
  private final AppsAppUserService appsAppUserService;
  private final AppUserPasswordService appUserPasswordService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EmailService emailService;
  private final AuthenvServiceConnector authenvServiceConnector;

  @GetMapping("/{appId}/validate_exit")
  public ResponseEntity<Void> validateAppUserExit(
      @PathVariable final String appId, @RequestParam final String toValidate) {
    final String redirectUrl = authenvServiceConnector.getRedirectUrls().getOrDefault(appId, "");
    try {
      appUserPasswordService.validateAndResetUser(appId, toValidate, true);
      return entityDtoConvertUtils.getResponseValidateUser(redirectUrl, true);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseValidateUser(redirectUrl, false);
    }
  }

  @GetMapping("/{appId}/reset_exit")
  public ResponseEntity<Void> resetAppUserMid(
      @PathVariable final String appId, @RequestParam final String toReset) {
    final String redirectUrl = authenvServiceConnector.getRedirectUrls().getOrDefault(appId, "");
    try {
      final String userToReset = appUserPasswordService.validateAndResetUser(appId, toReset, false);
      return entityDtoConvertUtils.getResponseResetUser(redirectUrl, true, userToReset);
    } catch (Exception ex) {
      return entityDtoConvertUtils.getResponseResetUser(redirectUrl, true, "");
    }
  }
}
