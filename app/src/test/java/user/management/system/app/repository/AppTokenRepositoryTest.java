package user.management.system.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.model.entity.AppTokenEntity;
import user.management.system.app.model.entity.AppUserEntity;

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
    assertEquals(appTokenEntityOptional.get().getRefreshToken(), "some-refresh-token");
  }

  @Test
  public void testFindByRefreshToken() {
    Optional<AppTokenEntity> appTokenEntityOptional =
        appTokenRepository.findByRefreshToken("some-refresh-token");

    assertTrue(appTokenEntityOptional.isPresent());
    assertEquals(appTokenEntityOptional.get().getAccessToken(), "some-access-token");
  }
}
