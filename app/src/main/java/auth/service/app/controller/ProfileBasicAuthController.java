package auth.service.app.controller;

import static auth.service.app.util.CommonUtils.getBaseUrlForLinkInEmail;
import static auth.service.app.util.JwtUtils.decodeAuthCredentials;

import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.TokenRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.service.AuditService;
import auth.service.app.service.EmailService;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.PlatformService;
import auth.service.app.service.ProfileService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.TokenService;
import auth.service.app.util.EntityDtoConvertUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ba_profile/platform")
@Validated
public class ProfileBasicAuthController {

  private final ProfileService profileService;
  private final PlatformService platformService;
  private final CircularDependencyService circularDependencyService;
  private final PlatformProfileRoleService platformProfileRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EmailService emailService;
  private final TokenService tokenService;
  private final AuditService auditService;

  @PostMapping("/{platformId}/create")
  public ResponseEntity<ProfileResponse> createProfile(
      @PathVariable final Long platformId,
      @Valid @RequestBody final ProfileRequest profileRequest,
      final HttpServletRequest request) {
    try {
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      final PlatformEntity platformEntity = circularDependencyService.readPlatform(platformId);
      final ProfileEntity profileEntity =
          profileService.createProfile(platformEntity, profileRequest, baseUrl);
      // TODO audit
      //      runAsync(
      //          () ->
      //              auditService.auditProfileCreate(
      //                  request, platformId, profileEntity, profileRequest.isGuestUser()));
      return entityDtoConvertUtils.getResponseSingleProfile(profileEntity);
    } catch (Exception ex) {
      log.error("Create Profile: [{}] | [{}]", platformId, profileRequest, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }

  @PostMapping("/{platformId}/login")
  public ResponseEntity<ProfilePasswordTokenResponse> loginProfile(
      @PathVariable final Long platformId,
      @Valid @RequestBody final ProfilePasswordRequest profilePasswordRequest,
      final HttpServletRequest request) {
    try {
      final ProfilePasswordTokenResponse profilePasswordTokenResponse =
          profileService.loginProfile(platformId, profilePasswordRequest);
      // TODO audit
      //      runAsync(
      //          () ->
      //              auditService.auditProfileLoginSuccess(
      //                  request, platformId, userLoginResponse.getUser().getId()));
      return ResponseEntity.ok(profilePasswordTokenResponse);
    } catch (Exception ex) {
      log.error("Login Profile: [{}] | [{}]", platformId, profilePasswordRequest, ex);
      // TODO audit
      //      runAsync(
      //          () ->
      //              auditService.auditProfileLoginFailure(
      //                  request, platformId, profilePasswordRequest.getEmail(), ex));
      return entityDtoConvertUtils.getResponseErrorProfilePassword(ex);
    }
  }

  @PostMapping("/{platformId}/token/refresh")
  public ResponseEntity<ProfilePasswordTokenResponse> refreshToken(
      @PathVariable final Long platformId,
      @Valid @RequestBody final TokenRequest tokenRequest,
      final HttpServletRequest request) {
    try {
      if (!StringUtils.hasText(tokenRequest.getRefreshToken())) {
        throw new ElementMissingException("Token", "Refresh");
      }

      Map<String, AuthToken> emailAuthToken = decodeAuthCredentials(tokenRequest.getRefreshToken());
      final TokenEntity tokenEntity =
          tokenService.readTokenByRefreshToken(tokenRequest.getRefreshToken());

      checkValidToken(platformId, emailAuthToken, tokenEntity);

      final ProfilePasswordTokenResponse profilePasswordTokenResponse =
          tokenService.saveToken(
              tokenEntity.getId(), null, tokenEntity.getPlatform(), tokenEntity.getProfile());
      // TODO audit
      //      runAsync(
      //          () ->
      //              auditService.auditProfileTokenRefreshSuccess(
      //                  request, platformId, appTokenEntity.getUser()));
      return ResponseEntity.ok(profilePasswordTokenResponse);
    } catch (Exception ex) {
      log.error("Refresh Token: [{}] | [{}]", platformId, tokenRequest, ex);
      // TODO audit
      //      runAsync(
      //          () ->
      //              auditService.auditProfileTokenRefreshFailure(
      //                  request, platformId, appTokenRequest.getProfileId(), ex));
      return entityDtoConvertUtils.getResponseErrorProfilePassword(ex);
    }
  }

  @PostMapping("/{platformId}/logout")
  public ResponseEntity<ResponseMetadata> logout(
      @PathVariable final Long platformId,
      @Valid @RequestBody final TokenRequest tokenRequest,
      final HttpServletRequest request) {
    try {
      if (!StringUtils.hasText(tokenRequest.getAccessToken())) {
        throw new ElementMissingException("Token", "Access");
      }

      Map<String, AuthToken> emailAuthToken = decodeAuthCredentials(tokenRequest.getRefreshToken());
      final TokenEntity tokenEntity =
          tokenService.readTokenByAccessToken(tokenRequest.getAccessToken());

      checkValidToken(platformId, emailAuthToken, tokenEntity);

      tokenService.saveToken(
          tokenEntity.getId(),
          LocalDateTime.now(),
          tokenEntity.getPlatform(),
          tokenEntity.getProfile());

      // TODO audit
      //      runAsync(
      //          () -> auditService.auditProfileLogoutSuccess(request, platformId,
      // tokenEntity.getUser()));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Logout: [{}] | [{}]", platformId, tokenRequest, ex);
      // TODO audit
      //      runAsync(
      //          () ->
      //              auditService.auditProfileLogoutFailure(
      //                  request, platformId, tokenRequest.getProfileId(), ex));
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @PostMapping("/{platformId}/reset")
  public ResponseEntity<ResponseMetadata> resetProfile(
      @PathVariable final Long platformId,
      @Valid @RequestBody final ProfilePasswordRequest profilePasswordRequest,
      final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity =
          profileService.resetProfile(platformId, profilePasswordRequest);
      // TODO audit
      // runAsync(() -> auditService.auditProfileResetSuccess(request, platformId, profileEntity));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Reset Profile: [{}] | [{}]", platformId, profilePasswordRequest, ex);
      // TODO audit
      //      runAsync(
      //          () ->
      //              auditService.auditProfileResetFailure(
      //                  request, platformId, profilePasswordRequest.getEmail(), ex));
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @GetMapping("/{platformId}/validate_init")
  public ResponseEntity<ResponseMetadata> validateProfileInit(
      @PathVariable final Long platformId,
      @RequestParam final String email,
      final HttpServletRequest request) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          profileService.readPlatformProfileRole(platformId, email);
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      emailService.sendProfileValidationEmail(
          platformProfileRoleEntity.getPlatform(), platformProfileRoleEntity.getProfile(), baseUrl);
      // TODO audit
      //      runAsync(
      //          () ->
      //              auditService.auditProfileValidateInit(
      //                  request, platformId, appsProfileEntity.getProfile()));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Validate Profile Init: [{}], [{}]", platformId, email, ex);
      // TODO audit
      // runAsync(() -> auditService.auditProfileValidateFailure(request, platformId, email, ex));
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @GetMapping("/{platformId}/reset_init")
  public ResponseEntity<ResponseMetadata> resetProfileInit(
      @PathVariable final Long platformId,
      @RequestParam final String email,
      final HttpServletRequest request) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          profileService.readPlatformProfileRole(platformId, email);
      final String baseUrl = getBaseUrlForLinkInEmail(request);
      emailService.sendProfileResetEmail(
          platformProfileRoleEntity.getPlatform(), platformProfileRoleEntity.getProfile(), baseUrl);
      // TODO audit
      //      runAsync(
      //          () -> auditService.auditProfileResetInit(request, platformId,
      // appsProfileEntity.getProfile()));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Reset Profile Init: [{}], [{}]", platformId, email, ex);
      // TODO audit
      // runAsync(() -> auditService.auditProfileResetFailure(request, platformId, email, ex));
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  private void checkValidToken(
      final Long platformId, Map<String, AuthToken> emailAuthToken, final TokenEntity tokenEntity) {
    Map.Entry<String, AuthToken> firstEntry = emailAuthToken.entrySet().iterator().next();
    String email = firstEntry.getKey();
    AuthToken authToken = firstEntry.getValue();

    if (!Objects.equals(email, tokenEntity.getProfile().getEmail())) {
      throw new JwtInvalidException("Profile Mismatch");
    }

    if (!Objects.equals(platformId, authToken.getPlatform().getId())) {
      throw new JwtInvalidException("Platform Mismatch");
    }

    if (tokenEntity.getDeletedDate() != null) {
      throw new JwtInvalidException("Deleted Token");
    }
  }
}
