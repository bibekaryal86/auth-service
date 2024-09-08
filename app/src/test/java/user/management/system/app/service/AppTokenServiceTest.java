package user.management.system.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import helper.TestData;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import user.management.system.BaseTest;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.UserLoginResponse;
import user.management.system.app.model.entity.AppTokenEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.repository.AppTokenRepository;

public class AppTokenServiceTest extends BaseTest {

  @Autowired private AppTokenService appTokenService;
  @Autowired private AppTokenRepository appTokenRepository;

  private static int id;
  private static AppUserEntity appUserEntity;

  @BeforeAll
  static void setUp(@Autowired AppTokenRepository appTokenRepository) {
    appUserEntity = TestData.getAppUserEntities().getFirst();

    AppTokenEntity appTokenEntity = new AppTokenEntity();
    appTokenEntity.setAccessToken("some-access-token");
    appTokenEntity.setRefreshToken("some-refresh-token");
    appTokenEntity.setUser(appUserEntity);
    appTokenRepository.save(appTokenEntity);

    appTokenEntity = new AppTokenEntity();
    appTokenEntity.setAccessToken("some-access-token-1");
    appTokenEntity.setRefreshToken("some-refresh-token-1");
    appTokenEntity.setUser(appUserEntity);
    appTokenEntity = appTokenRepository.save(appTokenEntity);
    id = appTokenEntity.getId();
  }

  @AfterAll
  static void tearDown(@Autowired AppTokenRepository appTokenRepository) {
    appTokenRepository.deleteAll();
  }

  @Test
  void testReadTokenByAccessToken() {
    AppTokenEntity appTokenEntity = appTokenService.readTokenByAccessToken("some-access-token");
    assertNotNull(appTokenEntity);
    assertEquals("some-refresh-token", appTokenEntity.getRefreshToken());
  }

  @Test
  void testReadTokenByAccessToken_NotFound() {
    assertThrows(
        ElementNotFoundException.class,
        () -> appTokenService.readTokenByAccessToken("some-non-existent-token"));
  }

  @Test
  void testReadTokenByRefreshToken() {
    AppTokenEntity appTokenEntity = appTokenService.readTokenByRefreshToken("some-refresh-token");
    assertNotNull(appTokenEntity);
    assertEquals("some-access-token", appTokenEntity.getAccessToken());
  }

  @Test
  void testReadTokenByRefreshToken_NotFound() {
    assertThrows(
        ElementNotFoundException.class,
        () -> appTokenService.readTokenByRefreshToken("some-non-existent-token"));
  }

  @Test
  void testSaveToken_Create() {
    UserLoginResponse userLoginResponse =
        appTokenService.saveToken(null, null, appUserEntity, "app-1");

    assertNotNull(userLoginResponse);
    assertNotNull(userLoginResponse.getAToken());
    assertNotNull(userLoginResponse.getRToken());
    assertNotNull(userLoginResponse.getUser());
    assertEquals(appUserEntity.getId(), userLoginResponse.getUser().getId());
  }

  @Test
  void testSaveToken_Update() {
    UserLoginResponse userLoginResponse =
        appTokenService.saveToken(id, null, appUserEntity, "app-1");
    assertNotNull(userLoginResponse);
    assertNotNull(userLoginResponse.getAToken());
    assertNotNull(userLoginResponse.getRToken());
    assertNotNull(userLoginResponse.getUser());
    assertNotEquals("some-access-token", userLoginResponse.getAToken());
    assertNotEquals("some-refresh-token", userLoginResponse.getRToken());
  }

  @Test
  void testSaveToken_Delete() {
    UserLoginResponse userLoginResponse =
        appTokenService.saveToken(id, LocalDateTime.now(), appUserEntity, "app-1");
    assertNotNull(userLoginResponse);
    assertNull(userLoginResponse.getAToken());
    assertNull(userLoginResponse.getRToken());
    assertNull(userLoginResponse.getUser());

    // validate deleted
    AppTokenEntity appTokenEntity = appTokenRepository.findById(id).orElse(null);
    assertNotNull(appTokenEntity);
    assertNotNull(appTokenEntity.getDeletedDate());
  }
}
