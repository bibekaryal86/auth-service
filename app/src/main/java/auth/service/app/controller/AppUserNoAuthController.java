package auth.service.app.controller;

import static auth.service.app.util.JwtUtils.decodeEmailAddressNoException;
import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.service.AppUserPasswordService;
import auth.service.app.service.AuditService;
import auth.service.app.util.EntityDtoConvertUtils;
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

@Hidden
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/na_app_users/user")
public class AppUserNoAuthController {

  private final AppUserPasswordService appUserPasswordService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EnvServiceConnector envServiceConnector;
  private final AuditService auditService;

  @GetMapping("/{appId}/validate_exit")
  public ResponseEntity<Void> validateAppUserExit(
      @PathVariable final String appId,
      @RequestParam final String toValidate,
      final HttpServletRequest request) {
    final String redirectUrl = envServiceConnector.getRedirectUrls().getOrDefault(appId, "");
    try {
      final AppUserEntity appUserEntity =
          appUserPasswordService.validateAndResetUser(appId, toValidate, true);
      runAsync(() -> auditService.auditAppUserValidateExit(request, appId, appUserEntity));
      return entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, true);
    } catch (Exception ex) {
      final String decodedEmail = decodeEmailAddressNoException(toValidate);
      log.error("Validate App User Exit: [{}], [{}]", appId, decodedEmail, ex);
      runAsync(() -> auditService.auditAppUserValidateFailure(request, appId, decodedEmail, ex));
      return entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, false);
    }
  }

  @GetMapping("/{appId}/reset_exit")
  public ResponseEntity<Void> resetAppUserExit(
      @PathVariable final String appId,
      @RequestParam final String toReset,
      final HttpServletRequest request) {
    final String redirectUrl = envServiceConnector.getRedirectUrls().getOrDefault(appId, "");
    try {
      final AppUserEntity appUserEntity =
          appUserPasswordService.validateAndResetUser(appId, toReset, false);
      runAsync(() -> auditService.auditAppUserResetExit(request, appId, appUserEntity));
      return entityDtoConvertUtils.getResponseResetProfile(
          redirectUrl, true, appUserEntity.getEmail());
    } catch (Exception ex) {
      final String decodedEmail = decodeEmailAddressNoException(toReset);
      log.error("Reset App User Exit: [{}], [{}]", appId, decodedEmail, ex);
      runAsync(() -> auditService.auditAppUserResetFailure(request, appId, decodedEmail, ex));
      return entityDtoConvertUtils.getResponseResetProfile(redirectUrl, false, "");
    }
  }
}
