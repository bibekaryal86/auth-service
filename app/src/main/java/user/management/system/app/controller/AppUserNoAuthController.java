package user.management.system.app.controller;

import static user.management.system.app.util.JwtUtils.decodeEmailAddressNoException;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.connector.AuthenvServiceConnector;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.service.AppUserPasswordService;
import user.management.system.app.service.AuditService;
import user.management.system.app.util.EntityDtoConvertUtils;

@Hidden
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/na_app_users/user")
public class AppUserNoAuthController {

  private final AppUserPasswordService appUserPasswordService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final AuthenvServiceConnector authenvServiceConnector;
  private final AuditService auditService;

  @GetMapping("/{appId}/validate_exit")
  public ResponseEntity<Void> validateAppUserExit(
      @PathVariable final String appId,
      @RequestParam final String toValidate,
      final HttpServletRequest request) {
    final String redirectUrl = authenvServiceConnector.getRedirectUrls().getOrDefault(appId, "");
    try {
      final AppUserEntity appUserEntity =
          appUserPasswordService.validateAndResetUser(appId, toValidate, true);
      auditService.auditAppUserValidateExit(request, appId, appUserEntity);
      return entityDtoConvertUtils.getResponseValidateUser(redirectUrl, true);
    } catch (Exception ex) {
      final String decodedEmail = decodeEmailAddressNoException(toValidate);
      log.error("Validate App User Exit: [{}], [{}]", appId, decodedEmail, ex);
      auditService.auditAppUserValidateFailure(request, appId, decodedEmail, ex);
      return entityDtoConvertUtils.getResponseValidateUser(redirectUrl, false);
    }
  }

  @GetMapping("/{appId}/reset_exit")
  public ResponseEntity<Void> resetAppUserExit(
      @PathVariable final String appId,
      @RequestParam final String toReset,
      final HttpServletRequest request) {
    final String redirectUrl = authenvServiceConnector.getRedirectUrls().getOrDefault(appId, "");
    try {
      final AppUserEntity appUserEntity =
          appUserPasswordService.validateAndResetUser(appId, toReset, false);
      auditService.auditAppUserResetExit(request, appId, appUserEntity);
      return entityDtoConvertUtils.getResponseResetUser(
          redirectUrl, true, appUserEntity.getEmail());
    } catch (Exception ex) {
      final String decodedEmail = decodeEmailAddressNoException(toReset);
      log.error("Reset App User Exit: [{}], [{}]", appId, decodedEmail, ex);
      auditService.auditAppUserResetFailure(request, appId, decodedEmail, ex);
      return entityDtoConvertUtils.getResponseResetUser(redirectUrl, false, "");
    }
  }
}
