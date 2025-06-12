package auth.service.app.controller;

import static auth.service.app.util.CommonUtils.getBaseUrlForLinkInEmail;
import static auth.service.app.util.CommonUtils.getIpAddress;
import static auth.service.app.util.JwtUtils.decodeAuthCredentials;
import static java.util.concurrent.CompletableFuture.runAsync;

import auth.service.app.connector.EnvServiceConnector;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.model.dto.ProfilePasswordRequest;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.dto.ProfileRequest;
import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.dto.TokenRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.model.enums.AuditEnums;
import auth.service.app.model.token.AuthToken;
import auth.service.app.service.AuditService;
import auth.service.app.service.CircularDependencyService;
import auth.service.app.service.EmailService;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.service.ProfileService;
import auth.service.app.service.TokenService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.dtos.ResponseWithMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
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
@RequestMapping("/api/v1/ba_profiles/platform")
@Validated
public class ProfileBasicAuthController {

  private final ProfileService profileService;
  private final CircularDependencyService circularDependencyService;
  private final PlatformProfileRoleService platformProfileRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final EmailService emailService;
  private final TokenService tokenService;
  private final AuditService auditService;
  private final EnvServiceConnector envServiceConnector;

  @PostMapping("/{platformId}/create")
  public ResponseEntity<ProfileResponse> createProfile(
      @PathVariable final Long platformId,
      @Valid @RequestBody final ProfileRequest profileRequest,
      final HttpServletRequest request) {
    try {
      String baseUrlForLinkInEmail = envServiceConnector.getBaseUrlForLinkInEmail();
      if (baseUrlForLinkInEmail == null) {
        baseUrlForLinkInEmail = getBaseUrlForLinkInEmail(request);
      }
      final PlatformEntity platformEntity =
          circularDependencyService.readPlatform(platformId, false);
      final ProfileEntity profileEntity =
          profileService.createProfile(platformEntity, profileRequest, baseUrlForLinkInEmail);
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_CREATE,
                  String.format(
                      "Profile Create [Id: %s] - [Email: %s]",
                      profileEntity.getId(), profileEntity.getEmail())));

      return ResponseEntity.ok(
          ProfileResponse.builder()
              .profiles(
                  List.of(
                      entityDtoConvertUtils.convertEntityToDtoProfileBasic(
                          profileEntity, platformId)))
              .responseMetadata(
                  new ResponseMetadata(
                      ResponseMetadata.emptyResponseStatusInfo(),
                      CommonUtils.defaultResponseCrudInfo(1, 0, 0, 0),
                      ResponseMetadata.emptyResponsePageInfo()))
              .build());
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
      final ProfileEntity profileEntity =
          profileService.readProfileByEmail(profilePasswordRequest.getEmail());
      final ProfilePasswordTokenResponse profilePasswordTokenResponse =
          profileService.loginProfile(platformId, profilePasswordRequest, getIpAddress(request));
      runAsync(
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

      return ResponseEntity.ok(profilePasswordTokenResponse);
    } catch (Exception ex) {
      log.error("Login Profile: [{}] | [{}]", platformId, profilePasswordRequest, ex);
      final ProfileEntity profileEntity =
          profileService.readProfileByEmailNoException(profilePasswordRequest.getEmail());

      // increase failed login attempts
      if (profileEntity != null) {
        int currentLoginAttempts =
            profileEntity.getLoginAttempts() == null ? 0 : profileEntity.getLoginAttempts();
        profileEntity.setLoginAttempts(currentLoginAttempts + 1);
        profileService.updateProfile(profileEntity);
      }

      runAsync(
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
              tokenEntity.getId(),
              null,
              tokenEntity.getPlatform(),
              tokenEntity.getProfile(),
              getIpAddress(request));
      runAsync(
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
      return ResponseEntity.ok(profilePasswordTokenResponse);
    } catch (Exception ex) {
      log.error("Refresh Token: [{}] | [{}]", platformId, tokenRequest, ex);
      final TokenEntity tokenEntity =
          tokenService.readTokenByRefreshTokenNoException(tokenRequest.getRefreshToken());
      final ProfileEntity profileEntity = tokenEntity == null ? null : tokenEntity.getProfile();
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.TOKEN_REFRESH_ERROR,
                  String.format(
                      "Profile Token Refresh Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      tokenRequest.getProfileId(),
                      profileEntity == null ? "N/A" : profileEntity.getEmail(),
                      platformId)));
      return entityDtoConvertUtils.getResponseErrorProfilePassword(ex);
    }
  }

  @PostMapping("/{platformId}/logout")
  public ResponseEntity<ResponseWithMetadata> logout(
      @PathVariable final Long platformId,
      @Valid @RequestBody final TokenRequest tokenRequest,
      final HttpServletRequest request) {
    try {
      if (!StringUtils.hasText(tokenRequest.getAccessToken())) {
        throw new ElementMissingException("Token", "Access");
      }

      Map<String, AuthToken> emailAuthToken = decodeAuthCredentials(tokenRequest.getAccessToken());
      final TokenEntity tokenEntity =
          tokenService.readTokenByAccessToken(tokenRequest.getAccessToken());

      checkValidToken(platformId, emailAuthToken, tokenEntity);

      tokenService.saveToken(
          tokenEntity.getId(),
          LocalDateTime.now(),
          tokenEntity.getPlatform(),
          tokenEntity.getProfile(),
          getIpAddress(request));

      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  tokenEntity.getProfile(),
                  AuditEnums.AuditProfile.PROFILE_LOGOUT,
                  String.format(
                      "Profile Logout [Id: %s] - [Email: %s] - [Platform: %s]",
                      tokenEntity.getProfile().getId(),
                      tokenEntity.getProfile().getEmail(),
                      tokenEntity.getPlatform().getId())));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Logout: [{}] | [{}]", platformId, tokenRequest, ex);
      final TokenEntity tokenEntity =
          tokenService.readTokenByAccessTokenNoException(tokenRequest.getAccessToken());
      final ProfileEntity profileEntity = tokenEntity == null ? null : tokenEntity.getProfile();
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_LOGOUT_ERROR,
                  String.format(
                      "Profile Logout Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      tokenRequest.getProfileId(),
                      profileEntity == null ? "N/A" : profileEntity.getEmail(),
                      platformId)));
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @GetMapping("/{platformId}/validate_init")
  public ResponseEntity<ResponseWithMetadata> validateProfileInit(
      @PathVariable final Long platformId,
      @RequestParam final String email,
      final HttpServletRequest request) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.readPlatformProfileRole(platformId, email);

      String baseUrlForLinkInEmail = envServiceConnector.getBaseUrlForLinkInEmail();
      if (baseUrlForLinkInEmail == null) {
        baseUrlForLinkInEmail = getBaseUrlForLinkInEmail(request);
      }
      emailService.sendProfileValidationEmail(
          platformProfileRoleEntity.getPlatform(),
          platformProfileRoleEntity.getProfile(),
          baseUrlForLinkInEmail);
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  platformProfileRoleEntity.getProfile(),
                  AuditEnums.AuditProfile.PROFILE_VALIDATE_INIT,
                  String.format(
                      "Profile Validate Init [Id: %s] - [Email: %s] - [Platform: %s]",
                      platformProfileRoleEntity.getProfile().getId(),
                      platformProfileRoleEntity.getProfile().getEmail(),
                      platformId)));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Validate Profile Init: [{}], [{}]", platformId, email, ex);
      final ProfileEntity profileEntity = profileService.readProfileByEmailNoException(email);
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_VALIDATE_ERROR,
                  String.format(
                      "Profile Validate Init Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileEntity == null
                          ? ConstantUtils.ELEMENT_ID_NOT_FOUND
                          : profileEntity.getId(),
                      email,
                      platformId)));
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @GetMapping("/{platformId}/reset_init")
  public ResponseEntity<ResponseWithMetadata> resetProfileInit(
      @PathVariable final Long platformId,
      @RequestParam final String email,
      final HttpServletRequest request) {
    try {
      final PlatformProfileRoleEntity platformProfileRoleEntity =
          platformProfileRoleService.readPlatformProfileRole(platformId, email);

      String baseUrlForLinkInEmail = envServiceConnector.getBaseUrlForLinkInEmail();
      if (baseUrlForLinkInEmail == null) {
        baseUrlForLinkInEmail = getBaseUrlForLinkInEmail(request);
      }
      emailService.sendProfileResetEmail(
          platformProfileRoleEntity.getPlatform(),
          platformProfileRoleEntity.getProfile(),
          baseUrlForLinkInEmail);
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  platformProfileRoleEntity.getProfile(),
                  AuditEnums.AuditProfile.PROFILE_RESET_INIT,
                  String.format(
                      "Profile Reset Init [Id: %s] - [Email: %s] - [Platform: %s]",
                      platformProfileRoleEntity.getProfile().getId(),
                      platformProfileRoleEntity.getProfile().getEmail(),
                      platformId)));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Reset Profile Init: [{}], [{}]", platformId, email, ex);
      final ProfileEntity profileEntity = profileService.readProfileByEmailNoException(email);
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_RESET_ERROR,
                  String.format(
                      "Profile Reset Init Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileEntity == null
                          ? ConstantUtils.ELEMENT_ID_NOT_FOUND
                          : profileEntity.getId(),
                      email,
                      platformId)));
      return entityDtoConvertUtils.getResponseErrorResponseMetadata(ex);
    }
  }

  @PostMapping("/{platformId}/reset")
  public ResponseEntity<ResponseWithMetadata> resetProfile(
      @PathVariable final Long platformId,
      @Valid @RequestBody final ProfilePasswordRequest profilePasswordRequest,
      final HttpServletRequest request) {
    try {
      final ProfileEntity profileEntity =
          profileService.resetProfile(platformId, profilePasswordRequest);
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_RESET_SUCCESS,
                  String.format(
                      "Profile Reset Success [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileEntity.getId(), profileEntity.getEmail(), platformId)));
      return ResponseEntity.noContent().build();
    } catch (Exception ex) {
      log.error("Reset Profile: [{}] | [{}]", platformId, profilePasswordRequest, ex);
      final ProfileEntity profileEntity =
          profileService.readProfileByEmailNoException(profilePasswordRequest.getEmail());
      runAsync(
          () ->
              auditService.auditProfile(
                  request,
                  profileEntity,
                  AuditEnums.AuditProfile.PROFILE_RESET_ERROR,
                  String.format(
                      "Profile Reset Error [Id: %s] - [Email: %s] - [Platform: %s]",
                      profileEntity == null
                          ? ConstantUtils.ELEMENT_ID_NOT_FOUND
                          : profileEntity.getId(),
                      profileEntity == null
                          ? profilePasswordRequest.getEmail()
                          : profileEntity.getEmail(),
                      platformId)));
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

    // if platform is deleted, it will throw exception
    circularDependencyService.readPlatform(platformId, false);
  }
}
