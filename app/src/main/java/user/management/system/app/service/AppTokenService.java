package user.management.system.app.service;

import static user.management.system.app.util.JwtUtils.encodeAuthCredentials;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppTokenEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.repository.AppTokenRepository;
import user.management.system.app.util.EntityDtoConvertUtils;

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
      final Integer id, final LocalDateTime deletedDate, final AppUserEntity appUserEntity) {
    log.debug("Save Token: [{}], [{}], [{}]", id, deletedDate, appUserEntity.getEmail());

    final AppUserDto appUserDto =
        entityDtoConvertUtils.convertEntityToDtoAppUser(appUserEntity, true);

    AppTokenEntity appTokenEntity = new AppTokenEntity();
    appTokenEntity.setUser(appUserEntity);
    appTokenEntity.setAccessToken(getNewAccessToken(appUserDto));
    appTokenEntity.setRefreshToken(getNewRefreshToken(appUserDto));

    if (id != null) {
      appTokenEntity.setId(id);

      if (deletedDate != null) {
        appTokenEntity.setDeletedDate(deletedDate);
      }
    }

    appTokenEntity = appTokenRepository.save(appTokenEntity);

    return UserLoginResponse.builder()
        .aToken(appTokenEntity.getAccessToken())
        .rToken(appTokenEntity.getRefreshToken())
        .user(appUserDto)
        .build();
  }

  // 15 minutes
  private String getNewAccessToken(final AppUserDto appUserDto) {
    return encodeAuthCredentials(appUserDto, 1000 * 60 * 15);
  }

  // 24 hours
  private String getNewRefreshToken(final AppUserDto appUserDto) {
    return encodeAuthCredentials(appUserDto, 1000 * 60 * 60 * 24);
  }
}
