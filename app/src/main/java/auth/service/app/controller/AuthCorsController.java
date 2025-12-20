package auth.service.app.controller;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.TokenInvalidException;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.EmailService;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.ProfileService;
import auth.service.app.service.TokenService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.CookieService;
import auth.service.app.util.EntityDtoConvertUtils;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/cors/platform")
@Validated
@CrossOrigin(origins = "${auth.cors.allowed_origins}", allowCredentials = "true")
public class AuthCorsController {

  private final ProfileService profileService;
  private final CircularDependencyService circularDependencyService;
  private final PlatformProfileRoleService platformProfileRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EmailService emailService;
  private final TokenService tokenService;
  private final CookieService cookieService;
  private final AuditService auditService;
  private final EnvServiceConnector envServiceConnector;

  @PostMapping("/{platformId}/login")
  public ResponseEntity<ProfilePasswordTokenResponse> login(
      @PathVariable final Long platformId,
      @Valid @RequestBody final ProfilePasswordRequest profilePasswordRequest,
      final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity =
          profileService.readProfileByEmail(profilePasswordRequest.getEmail());
      final ProfilePasswordTokenResponse profilePasswordTokenResponse =
          profileService.loginProfile(
              platformId, profilePasswordRequest, CommonUtils.getIpAddress(request));
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_LOGIN,
                  String.format(
                      "Profile Login [Id: %s] - [Email: %s]",
                      profileEntity.getId(), profileEntity.getEmail())));

      // reset login attempts and set last login date
      profileEntity.setLoginAttempts(0);
      profileEntity.setLastLogin(LocalDateTime.now());
      profileService.updateProfile(profileEntity);

      final ResponseCookie refreshTokenCookie =
          cookieService.buildRefreshCookie(
              profilePasswordTokenResponse.getRefreshToken(),
              ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS);
      final ResponseCookie csrfTokenCookie =
          cookieService.buildCsrfCookie(
              profilePasswordTokenResponse.getCsrfToken(),
              ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS);

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
          .header(HttpHeaders.SET_COOKIE, csrfTokenCookie.toString())
          .body(profilePasswordTokenResponse);
    } catch (Exception ex) {
      log.error(
          "Login Profile: PlatformId=[{}], ProfilePasswordRequest=[{}]",
          platformId,
          profilePasswordRequest,
          ex);
      final ProfileEntity profileEntity =
          profileService.readProfileByEmailNoException(profilePasswordRequest.getEmail());

      // increase failed login attempts
      if (profileEntity != null) {
        int currentLoginAttempts =
            profileEntity.getLoginAttempts() == null ? 0 : profileEntity.getLoginAttempts();
        profileEntity.setLoginAttempts(currentLoginAttempts + 1);
        profileService.updateProfile(profileEntity);
      }

      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_LOGIN_ERROR,
                  String.format(
                      "Profile Login Error [Id: %s] - [Email: %s]",
                      profileEntity == null
                          ? ConstantUtils.ELEMENT_ID_NOT_FOUND
                          : profileEntity.getId(),
                      profilePasswordRequest.getEmail())));
      return entityDtoConvertUtils.getResponseErrorProfilePassword(ex);
    }
  }

  @GetMapping("/{platformId}/profile/{profileId}/refresh")
  public ResponseEntity<ProfilePasswordTokenResponse> refresh(
      @PathVariable final Long platformId,
      @PathVariable final Long profileId,
      final HttpServletRequest request) {
    try {
      final String refreshTokenRequest =
          cookieService.getCookieValue(request, ConstantUtils.COOKIE_REFRESH_TOKEN);
      final String csrfTokenCookieRequest =
          cookieService.getCookieValue(request, ConstantUtils.COOKIE_CSRF_TOKEN);
      final String csrfTokenHeaderRequest = request.getHeader(ConstantUtils.HEADER_CSRF_TOKEN);

      if (CommonUtilities.isEmpty(refreshTokenRequest)) {
        throw new ProfileNotAuthorizedException("Token Mismatch/Invalid...");
      }

      if (CommonUtilities.isEmpty(csrfTokenCookieRequest)
          || CommonUtilities.isEmpty(csrfTokenHeaderRequest)
          || !csrfTokenCookieRequest.equals(csrfTokenHeaderRequest)) {
        throw new ProfileNotAuthorizedException("Token Invalid/Mismatch...");
      }

      final TokenEntity tokenEntity = tokenService.readTokenByRefreshToken(refreshTokenRequest);

      // method will throw exception if invalid
      validateToken(platformId, profileId, tokenEntity);

      final ProfilePasswordTokenResponse profilePasswordTokenResponse =
          tokenService.saveToken(
              tokenEntity.getId(),
              null,
              tokenEntity.getPlatform(),
              tokenEntity.getProfile(),
              CommonUtils.getIpAddress(request));

      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  tokenEntity.getProfile(),
                  AuditEnums.AuditProfile.TOKEN_REFRESH,
                  String.format(
                      "Profile Token Refresh [Id: %s] - [Email: %s] - [Platform: %s]",
                      tokenEntity.getProfile().getId(),
                      tokenEntity.getProfile().getEmail(),
                      tokenEntity.getPlatform().getId())));

      final ResponseCookie refreshTokenCookieResponse =
          cookieService.buildRefreshCookie(
              profilePasswordTokenResponse.getRefreshToken(),
              ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS);
      final ResponseCookie csrfTokenCookieResponse =
          cookieService.buildCsrfCookie(
              profilePasswordTokenResponse.getCsrfToken(),
              ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS);

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, refreshTokenCookieResponse.toString())
          .header(HttpHeaders.SET_COOKIE, csrfTokenCookieResponse.toString())
          .body(profilePasswordTokenResponse);
    } catch (Exception ex) {
      log.error("Refresh Token: PlatformId=[{}], ProfileId=[{}]", platformId, profileId, ex);
      final ProfileEntity profileEntity =
          circularDependencyService.readProfileNoException(profileId, Boolean.TRUE);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR,
                  String.format(
                      "Profile Token Refresh Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileId,
                      profileEntity == null ? "N/A" : profileEntity.getEmail(),
                      platformId)));
      return entityDtoConvertUtils.getResponseErrorProfilePassword(ex);
    }
  }

  @GetMapping("/{platformId}/profile/{profileId}/logout")
  public ResponseEntity<ProfilePasswordTokenResponse> logout(
      @PathVariable final Long platformId,
      @PathVariable final Long profileId,
      final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity =
          circularDependencyService.readProfile(profileId, Boolean.TRUE, Boolean.FALSE);
      tokenService.setTokenDeletedDateByProfileId(profileId);

      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_LOGOUT,
                  String.format(
                      "Profile Logout [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileId, profileEntity.getEmail(), platformId)));

      final ResponseCookie refreshTokenCookieResponse = cookieService.buildRefreshCookie("", 0);
      final ResponseCookie csrfTokenCookieResponse = cookieService.buildCsrfCookie("", 0);

      return ResponseEntity.noContent()
          .header(HttpHeaders.SET_COOKIE, refreshTokenCookieResponse.toString())
          .header(HttpHeaders.SET_COOKIE, csrfTokenCookieResponse.toString())
          .build();
    } catch (Exception ex) {
      log.error("Logout: PlatformId=[{}], ProfileId=[{}]", platformId, profileId, ex);
      final ProfileEntity profileEntity =
          circularDependencyService.readProfileNoException(profileId, Boolean.TRUE);
      CompletableFuture.runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR,
                  String.format(
                      "Profile Logout Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileId,
                      profileEntity == null ? "N/A" : profileEntity.getEmail(),
                      platformId)));
      return entityDtoConvertUtils.getResponseErrorProfilePassword(ex);
    }
  }

  private void validateToken(
      final Long platformId, final Long profileId, final TokenEntity tokenEntity) {
    if (!Objects.equals(platformId, tokenEntity.getPlatform().getId())) {
      throw new ProfileNotAuthorizedException("Platform Mismatch...");
    }

    if (!Objects.equals(profileId, tokenEntity.getProfile().getId())) {
      throw new ProfileNotAuthorizedException("Profile Mismatch...");
    }

    if (tokenEntity.getDeletedDate() != null) {
      throw new TokenInvalidException("Deleted Token...");
    }

    if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now().minusSeconds(60L))) {
      throw new TokenInvalidException("Expired Token...");
    }

    // if platform is deleted, it will throw exception
    circularDependencyService.readPlatform(platformId, Boolean.FALSE);
    // if profile is deleted, it will throw exception
    circularDependencyService.readProfile(profileId, Boolean.FALSE, Boolean.FALSE);
  }
}
