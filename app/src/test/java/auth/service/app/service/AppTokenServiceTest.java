package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.UserLoginResponse;
import auth.service.app.model.entity.AppTokenEntity;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.repository.AppTokenRepository;
import helper.TestData;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        appTokenService.saveToken(null, null, appUserEntity, "APP_ID");

    assertNotNull(userLoginResponse);
    assertNotNull(userLoginResponse.getAToken());
    assertNotNull(userLoginResponse.getRToken());
    assertNotNull(userLoginResponse.getUser());
    assertEquals(appUserEntity.getId(), userLoginResponse.getUser().getId());

    // cleanup
    AppTokenEntity appTokenEntity =
        appTokenRepository.findByAccessToken(userLoginResponse.getAToken()).orElse(null);
    assertNotNull(appTokenEntity);
    appTokenRepository.deleteById(appTokenEntity.getId());
  }

  @Test
  void testSaveToken_Update() {
    UserLoginResponse userLoginResponse =
        appTokenService.saveToken(id, null, appUserEntity, "APP_ID");
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
        appTokenService.saveToken(id, LocalDateTime.now(), appUserEntity, "APP_ID");
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
