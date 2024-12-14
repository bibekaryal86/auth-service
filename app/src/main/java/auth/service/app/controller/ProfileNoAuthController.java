package auth.service.app.controller;

import static auth.service.app.util.JwtUtils.decodeEmailAddressNoException;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.ProfileService;
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
@RequestMapping("/api/v1/na_profile/platform")
public class ProfileNoAuthController {

  private final CircularDependencyService circularDependencyService;
  private final ProfileService profileService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EnvServiceConnector envServiceConnector;
  private final AuditService auditService;

  @GetMapping("/{platformId}/validate_exit")
  public ResponseEntity<Void> validateProfileExit(
      @PathVariable final Long platformId,
      @RequestParam final String toValidate,
      final HttpServletRequest request) {
    String redirectUrl = String.format("RedirectUrl-%s", platformId);
    try {
      final PlatformEntity platformEntity = circularDependencyService.readPlatform(platformId);
      redirectUrl =
          envServiceConnector.getRedirectUrls().getOrDefault(platformEntity.getPlatformName(), "");
      final ProfileEntity profileEntity =
          profileService.validateAndResetProfile(platformId, toValidate, true);

      // TODO audit
      // runAsync(() -> auditService.auditAppUserValidateExit(request, appId, appUserEntity));
      return entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, true);
    } catch (Exception ex) {
      final String decodedEmail = decodeEmailAddressNoException(toValidate);
      log.error("Validate Profile Exit: [{}], [{}]", platformId, decodedEmail, ex);
      // TODO audit
      // runAsync(() -> auditService.auditAppUserValidateFailure(request, appId, decodedEmail, ex));
      return entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, false);
    }
  }

  @GetMapping("/{platformId}/reset_exit")
  public ResponseEntity<Void> resetProfileExit(
      @PathVariable final Long platformId,
      @RequestParam final String toReset,
      final HttpServletRequest request) {
    String redirectUrl = String.format("RedirectUrl-%s", platformId);
    try {
      final PlatformEntity platformEntity = circularDependencyService.readPlatform(platformId);
      redirectUrl =
          envServiceConnector.getRedirectUrls().getOrDefault(platformEntity.getPlatformName(), "");
      final ProfileEntity profileEntity =
          profileService.validateAndResetProfile(platformId, toReset, false);
      // TODO audit
      // runAsync(() -> auditService.auditAppUserResetExit(request, appId, appUserEntity));
      return entityDtoConvertUtils.getResponseResetProfile(
          redirectUrl, true, profileEntity.getEmail());
    } catch (Exception ex) {
      final String decodedEmail = decodeEmailAddressNoException(toReset);
      log.error("Reset Profile Exit: [{}], [{}]", platformId, decodedEmail, ex);
      // TODO audit
      // runAsync(() -> auditService.auditAppUserResetFailure(request, appId, decodedEmail, ex));
      return entityDtoConvertUtils.getResponseResetProfile(redirectUrl, false, "");
    }
  }
}
