package auth.service.app.service;

import static auth.service.app.util.JwtUtils.encodeAuthCredentials;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppUserDto;
import auth.service.app.model.dto.UserLoginResponse;
import auth.service.app.model.entity.AppTokenEntity;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.repository.AppTokenRepository;
import auth.service.app.util.EntityDtoConvertUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppTokenService {

  private final AppTokenRepository appTokenRepository;
  private final EntityDtoConvertUtils entityDtoConvertUtils;

  // CREATE
  // handled by save

  // READ
  public AppTokenEntity readTokenByAccessToken(final String accessToken) {
    return appTokenRepository
        .findByAccessToken(accessToken)
        .orElseThrow(() -> new ElementNotFoundException("Token", "access"));
  }

  public AppTokenEntity readTokenByRefreshToken(final String refreshToken) {
    return appTokenRepository
        .findByRefreshToken(refreshToken)
        .orElseThrow(() -> new ElementNotFoundException("Token", "refresh"));
  }

  // UPDATE
  // handled by save

  // DELETE
  // only soft delete, handled by update

  // RESTORE
  // not available

  public UserLoginResponse saveToken(
      final Integer id,
      final LocalDateTime deletedDate,
      final AppUserEntity appUserEntity,
      final String appId) {
    log.debug("Save Token: [{}], [{}], [{}]", id, deletedDate, appUserEntity.getEmail());

    if (id != null && deletedDate != null) {
      appTokenRepository
          .findById(id)
          .ifPresent(
              toDelete -> {
                toDelete.setDeletedDate(deletedDate);
                appTokenRepository.save(toDelete);
              });
      return UserLoginResponse.builder().build();
    }

    final AppUserDto appUserDto =
        entityDtoConvertUtils.convertEntityToDtoAppUser(appUserEntity, true);

    AppTokenEntity appTokenEntity = new AppTokenEntity();
    appTokenEntity.setUser(appUserEntity);
    appTokenEntity.setAccessToken(getNewAccessToken(appId, appUserDto));
    appTokenEntity.setRefreshToken(getNewRefreshToken(appId, appUserDto));

    if (id != null) {
      appTokenEntity.setId(id);
    }

    appTokenEntity = appTokenRepository.save(appTokenEntity);

    return UserLoginResponse.builder()
        .aToken(appTokenEntity.getAccessToken())
        .rToken(appTokenEntity.getRefreshToken())
        .user(appUserDto)
        .build();
  }

  // 15 minutes
  private String getNewAccessToken(final String appId, final AppUserDto appUserDto) {
    return encodeAuthCredentials(appId, appUserDto, 1000 * 60 * 15);
  }

  // 24 hours
  private String getNewRefreshToken(final String appId, final AppUserDto appUserDto) {
    return encodeAuthCredentials(appId, appUserDto, 1000 * 60 * 60 * 24);
  }
}
