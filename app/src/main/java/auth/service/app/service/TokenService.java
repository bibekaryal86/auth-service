package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.repository.TokenRepository;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import auth.service.app.util.JwtUtils;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
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

  // READ
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

  // DELETE
  // only soft delete, handled by update

  // RESTORE
  // not available

  // OTHERS

  public int setTokenDeletedDateByProfileId(final long profileId) {
    log.debug("Set Token Deleted Date by Profile Id: ProfileId=[{}]", profileId);
    final boolean isTest = environment.matchesProfiles(ConstantUtils.ENV_SPRINGBOOTTEST);
    return isTest
        ? tokenRepository.setTokensAsDeletedByProfileIdTest(profileId)
        : tokenRepository.setTokensAsDeletedByProfileId(profileId);
  }

  public ProfilePasswordTokenResponse saveToken(
      final Long id,
      final LocalDateTime deletedDate,
      final PlatformEntity platformEntity,
      final ProfileEntity profileEntity,
      final String ipAddress) {
    log.debug(
        "Save Token: Id=[{}], DeletedDate=[{}], PlatformId=[{}], Email=[{}], IpAddress=[{}]",
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
      return ProfilePasswordTokenResponse.builder()
          .responseMetadata(ResponseMetadata.emptyResponseMetadata())
          .build();
    }

    final AuthToken authToken =
        entityDtoConvertUtils.getAuthTokenFromProfile(platformEntity, profileEntity);
    final String accessToken =
        JwtUtils.encodeAuthCredentials(authToken, ConstantUtils.ACCESS_TOKEN_VALIDITY_MILLISECONDS);
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

    final boolean isSandbox = environment.matchesProfiles(ConstantUtils.ENV_SANDBOX);

    return ProfilePasswordTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(isSandbox ? refreshToken : null)
        .csrfToken(isSandbox ? csrfToken : null)
        .authToken(authToken)
        .responseMetadata(ResponseMetadata.emptyResponseMetadata())
        .build();
  }

  private String generateSecureToken() {
    byte[] bytes = new byte[ConstantUtils.TOKEN_LENGTH];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
