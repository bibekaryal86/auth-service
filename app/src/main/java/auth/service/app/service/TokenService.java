package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.repository.TokenRepository;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import auth.service.app.util.JwtUtils;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

  private final TokenRepository tokenRepository;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final Environment environment;

  private final SecureRandom secureRandom = new SecureRandom();

  // CREATE
  // handled by save

  public TokenEntity readTokenByRefreshToken(final String refreshToken) {
    return tokenRepository
        .findByRefreshToken(refreshToken)
        .orElseThrow(() -> new ElementNotFoundException("Token", "refresh"));
  }

  public TokenEntity readTokenByRefreshTokenNoException(final String refreshToken) {
    try {
      return readTokenByRefreshToken(refreshToken);
    } catch (Exception ignored) {
      return null;
    }
  }

  // UPDATE
  // handled by save

  public int setTokenDeletedDateByProfileId(final long profileId) {
    log.debug("Set Token Deleted Date by Profile Id: [{}]", profileId);
    final boolean isTest = environment.matchesProfiles("springboottest");
    return isTest
        ? tokenRepository.setTokensAsDeletedByProfileIdTest(profileId)
        : tokenRepository.setTokensAsDeletedByProfileId(profileId);
  }

  // DELETE
  // only soft delete, handled by update

  // RESTORE
  // not available

  public ProfilePasswordTokenResponse saveToken(
      final Long id,
      final LocalDateTime deletedDate,
      final PlatformEntity platformEntity,
      final ProfileEntity profileEntity,
      final String ipAddress) {
    log.debug(
        "Save Token: [{}], [{}], [{}], [{}], [{}]",
        id,
        deletedDate,
        platformEntity.getId(),
        profileEntity.getEmail(),
        ipAddress);

    if (id != null && deletedDate != null) {
      tokenRepository
          .findById(id)
          .ifPresent(
              toDelete -> {
                toDelete.setDeletedDate(deletedDate);
                tokenRepository.save(toDelete);
              });
      return ProfilePasswordTokenResponse.builder().build();
    }

    final ProfileDto profileDto =
        entityDtoConvertUtils.convertEntityToDtoProfileBasic(profileEntity, platformEntity.getId());

    final String accessToken =
        JwtUtils.encodeAuthCredentials(
            platformEntity, profileDto, ConstantUtils.ACCESS_TOKEN_VALIDITY_MILLISECONDS);
    final String refreshToken = generateSecureToken();
    final String csrfToken = generateSecureToken();

    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setPlatform(platformEntity);
    tokenEntity.setProfile(profileEntity);
    tokenEntity.setIpAddress(ipAddress);
    tokenEntity.setRefreshToken(refreshToken);
    tokenEntity.setCsrfToken(csrfToken);
    tokenEntity.setExpiryDate(
        LocalDateTime.now().plusSeconds(ConstantUtils.REFRESH_TOKEN_VALIDITY_SECONDS));

    if (id != null) {
      tokenEntity.setId(id);
    }

    tokenRepository.save(tokenEntity);

    return ProfilePasswordTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .csrfToken(csrfToken)
        .profile(profileDto)
        .build();
  }

  private String generateSecureToken() {
    byte[] bytes = new byte[ConstantUtils.TOKEN_LENGTH];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
