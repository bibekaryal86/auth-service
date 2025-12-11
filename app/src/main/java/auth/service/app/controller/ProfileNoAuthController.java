package auth.service.app.controller;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.ProfileService;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import auth.service.app.util.JwtUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
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
@RequestMapping("/api/v1/na_profiles/platform")
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
      final PlatformEntity platformEntity =
          circularDependencyService.readPlatform(platformId, Boolean.FALSE);
      redirectUrl =
          envServiceConnector.getRedirectUrls().getOrDefault(platformEntity.getPlatformName(), "");
      final ProfileEntity profileEntity =
          profileService.validateAndResetProfile(platformId, toValidate, Boolean.TRUE);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_VALIDATE_EXIT,
                  String.format(
                      "Profile Validate Exit [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileEntity.getId(), profileEntity.getEmail(), platformId)));
      return entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, Boolean.TRUE);
    } catch (Exception ex) {
      final String decodedEmail = JwtUtils.decodeEmailAddressNoException(toValidate);
      log.error(
          "Validate Profile Exit: PlatformId=[{}], DecodedEmail=[{}]",
          platformId,
          decodedEmail,
          ex);
      final ProfileEntity profileEntity =
          profileService.readProfileByEmailNoException(decodedEmail);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_VALIDATE_ERROR,
                  String.format(
                      "Profile Validate Exit Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileEntity == null
                          ? ConstantUtils.ELEMENT_ID_NOT_FOUND
                          : profileEntity.getId(),
                      profileEntity == null ? decodedEmail : profileEntity.getEmail(),
                      platformId)));
      return entityDtoConvertUtils.getResponseValidateProfile(redirectUrl, Boolean.FALSE);
    }
  }

  @GetMapping("/{platformId}/reset_exit")
  public ResponseEntity<Void> resetProfileExit(
      @PathVariable final Long platformId,
      @RequestParam final String toReset,
      final HttpServletRequest request) {
    String redirectUrl = String.format("RedirectUrl-%s", platformId);
    try {
      final PlatformEntity platformEntity =
          circularDependencyService.readPlatform(platformId, Boolean.FALSE);
      redirectUrl =
          envServiceConnector.getRedirectUrls().getOrDefault(platformEntity.getPlatformName(), "");
      final ProfileEntity profileEntity =
          profileService.validateAndResetProfile(platformId, toReset, Boolean.FALSE);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_RESET_EXIT,
                  String.format(
                      "Profile Reset Exit [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileEntity.getId(), profileEntity.getEmail(), platformId)));
      return entityDtoConvertUtils.getResponseResetProfile(
          redirectUrl, Boolean.TRUE, profileEntity.getEmail());
    } catch (Exception ex) {
      final String decodedEmail = JwtUtils.decodeEmailAddressNoException(toReset);
      log.error(
          "Reset Profile Exit: PlatformId=[{}], DecodedEmail=[{}]", platformId, decodedEmail, ex);
      final ProfileEntity profileEntity =
          profileService.readProfileByEmailNoException(decodedEmail);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_RESET_ERROR,
                  String.format(
                      "Profile Reset Exit Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileEntity == null
                          ? ConstantUtils.ELEMENT_ID_NOT_FOUND
                          : profileEntity.getId(),
                      profileEntity == null ? decodedEmail : profileEntity.getEmail(),
                      platformId)));
      return entityDtoConvertUtils.getResponseResetProfile(redirectUrl, false, "");
    }
  }
}
