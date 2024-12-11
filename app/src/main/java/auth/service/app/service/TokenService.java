package auth.service.app.service;

import static auth.service.app.util.JwtUtils.encodeAuthCredentials;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.repository.TokenRepository;
import auth.service.app.util.EntityDtoConvertUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

  private final TokenRepository tokenRepository;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  // CREATE
  // handled by save

  // READ
  public TokenEntity readTokenByAccessToken(final String accessToken) {
    return tokenRepository
        .findByAccessToken(accessToken)
        .orElseThrow(() -> new ElementNotFoundException("Token", "access"));
  }

  public TokenEntity readTokenByRefreshToken(final String refreshToken) {
    return tokenRepository
        .findByRefreshToken(refreshToken)
        .orElseThrow(() -> new ElementNotFoundException("Token", "refresh"));
  }

  // UPDATE
  // handled by save

  // DELETE
  // only soft delete, handled by update

  // RESTORE
  // not available

  public ProfilePasswordTokenResponse saveToken(
      final Long id,
      final LocalDateTime deletedDate,
      final PlatformEntity platformEntity,
      final ProfileEntity profileEntity) {
    log.debug(
        "Save Token: [{}], [{}], [{}], [{}]",
        id,
        deletedDate,
        platformEntity.getId(),
        profileEntity.getEmail());

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
        entityDtoConvertUtils.convertEntityToDtoProfile(profileEntity, true);

    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setPlatform(platformEntity);
    tokenEntity.setProfile(profileEntity);
    tokenEntity.setAccessToken(getNewAccessToken(platformEntity, profileDto));
    tokenEntity.setRefreshToken(getNewRefreshToken(platformEntity, profileDto));

    if (id != null) {
      tokenEntity.setId(id);
    }

    tokenEntity = tokenRepository.save(tokenEntity);

    return ProfilePasswordTokenResponse.builder()
        .aToken(tokenEntity.getAccessToken())
        .rToken(tokenEntity.getRefreshToken())
        .profile(profileDto)
        .build();
  }

  // 15 minutes
  private String getNewAccessToken(
      final PlatformEntity platformEntity, final ProfileDto profileDto) {
    return encodeAuthCredentials(platformEntity, profileDto, 1000 * 60 * 15);
  }

  // 24 hours
  private String getNewRefreshToken(
      final PlatformEntity platformEntity, final ProfileDto profileDto) {
    return encodeAuthCredentials(platformEntity, profileDto, 1000 * 60 * 60 * 24);
  }
}
