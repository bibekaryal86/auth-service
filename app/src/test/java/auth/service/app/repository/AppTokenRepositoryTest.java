package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.entity.AppTokenEntity;
import auth.service.app.model.entity.AppUserEntity;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppTokenRepositoryTest extends BaseTest {

  @Autowired private AppTokenRepository appTokenRepository;

  @BeforeAll
  static void setUp(@Autowired AppTokenRepository appTokenRepository) {
    AppTokenEntity appTokenEntity = new AppTokenEntity();
    appTokenEntity.setAccessToken("some-access-token");
    appTokenEntity.setRefreshToken("some-refresh-token");
    AppUserEntity appUserEntity = new AppUserEntity();
    appUserEntity.setId(4);
    appTokenEntity.setUser(appUserEntity);
    appTokenRepository.save(appTokenEntity);
  }

  @AfterAll
  static void tearDown(@Autowired AppTokenRepository appTokenRepository) {
    appTokenRepository.deleteAll();
  }

  @Test
  public void testFindByAccessToken() {
    Optional<AppTokenEntity> appTokenEntityOptional =
        appTokenRepository.findByAccessToken("some-access-token");

    assertTrue(appTokenEntityOptional.isPresent());
    assertEquals("some-refresh-token", appTokenEntityOptional.get().getRefreshToken());
  }

  @Test
  public void testFindByRefreshToken() {
    Optional<AppTokenEntity> appTokenEntityOptional =
        appTokenRepository.findByRefreshToken("some-refresh-token");

    assertTrue(appTokenEntityOptional.isPresent());
    assertEquals("some-access-token", appTokenEntityOptional.get().getAccessToken());
  }
}
