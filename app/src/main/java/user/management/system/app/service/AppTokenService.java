package user.management.system.app.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.entity.AppTokenEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.repository.AppTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppTokenService {

  private final AppTokenRepository appTokenRepository;

  // CREATE
  // handled by save

  public AppTokenEntity saveToken(
      final Integer id,
      final LocalDateTime deletedDate,
      final AppUserEntity appUserEntity,
      final String accessToken,
      final String refreshToken) {
    log.debug("Save Token: [{}], [{}], [{}]", id, deletedDate, appUserEntity.getEmail());
    AppTokenEntity appTokenEntity = new AppTokenEntity();
    appTokenEntity.setUser(appUserEntity);
    appTokenEntity.setAccessToken(accessToken);
    appTokenEntity.setRefreshToken(refreshToken);

    if (id != null) {
      appTokenEntity.setId(id);

      if (deletedDate != null) {
        appTokenEntity.setDeletedDate(deletedDate);
      }
    }

    return appTokenRepository.save(appTokenEntity);
  }

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
}
