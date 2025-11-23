package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.ProfilePasswordTokenResponse;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import auth.service.app.repository.TokenRepository;
import helper.TestData;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TokenServiceTest extends BaseTest {

  @Autowired private TokenService tokenService;
  @Autowired private TokenRepository tokenRepository;

  private static Long tokenId;
  private static PlatformEntity platformEntity;
  private static ProfileEntity profileEntity;

  @BeforeAll
  static void setUp(@Autowired TokenRepository tokenRepository) {
    profileEntity = TestData.getProfileEntities().getFirst();
    platformEntity = TestData.getPlatformEntities().getFirst();

    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setPlatform(platformEntity);
    tokenEntity.setProfile(profileEntity);
    tokenEntity.setRefreshToken("some-refresh-token");
    tokenEntity.setCsrfToken("some-csrf-token");
    tokenEntity.setIpAddress("some-ip-address");
    tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(1L));
    tokenRepository.save(tokenEntity);

    tokenEntity = new TokenEntity();
    tokenEntity.setPlatform(platformEntity);
    tokenEntity.setProfile(profileEntity);
    tokenEntity.setRefreshToken("some-refresh-token-1");
    tokenEntity.setCsrfToken("some-csrf-token-1");
    tokenEntity.setIpAddress("some-ip-address-1");
    tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(1L));
    tokenEntity = tokenRepository.save(tokenEntity);
    tokenId = tokenEntity.getId();
  }

  @AfterAll
  static void tearDown(@Autowired TokenRepository tokenRepository) {
    tokenRepository.deleteAll();
  }

  @Test
  void testReadTokenByRefreshToken() {
    TokenEntity tokenEntity = tokenService.readTokenByRefreshToken("some-refresh-token");
    assertNotNull(tokenEntity);
    assertEquals("some-csrf-token", tokenEntity.getCsrfToken());
  }

  @Test
  void testReadTokenByRefreshToken_NotFound() {
    assertThrows(
        ElementNotFoundException.class,
        () -> tokenService.readTokenByRefreshToken("some-non-existent-token"));
  }

  @Test
  void testReadTokenByRefreshTokenNoException() {
    assertDoesNotThrow(
        () -> tokenService.readTokenByRefreshTokenNoException("some-non-existent-token"));
  }

  @Test
  void testSaveToken_Create() {
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        tokenService.saveToken(null, null, platformEntity, profileEntity, "some-ip-address");

    assertNotNull(profilePasswordTokenResponse);
    assertNotNull(profilePasswordTokenResponse.getAccessToken());
    assertNotNull(profilePasswordTokenResponse.getRefreshToken());
    assertNotNull(profilePasswordTokenResponse.getCsrfToken());
    assertNotNull(profilePasswordTokenResponse.getProfile());
    assertEquals(profileEntity.getId(), profilePasswordTokenResponse.getProfile().getId());

    // cleanup
    TokenEntity tokenEntity =
        tokenRepository
            .findByRefreshToken(profilePasswordTokenResponse.getRefreshToken())
            .orElse(null);
    assertNotNull(tokenEntity);
    tokenRepository.deleteById(tokenEntity.getId());
  }

  @Test
  void testSaveToken_Update() {
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        tokenService.saveToken(tokenId, null, platformEntity, profileEntity, "some-ip-address");
    assertNotNull(profilePasswordTokenResponse);
    assertNotNull(profilePasswordTokenResponse.getRefreshToken());
    assertNotNull(profilePasswordTokenResponse.getCsrfToken());
    assertNotNull(profilePasswordTokenResponse.getProfile());
    assertNotEquals("some-refresh-token", profilePasswordTokenResponse.getRefreshToken());
    assertNotEquals("some-csrf-token", profilePasswordTokenResponse.getCsrfToken());
  }

  @Test
  void testSaveToken_Delete() {
    ProfilePasswordTokenResponse profilePasswordTokenResponse =
        tokenService.saveToken(
            tokenId, LocalDateTime.now(), platformEntity, profileEntity, "some-ip-address");
    assertNotNull(profilePasswordTokenResponse);
    assertNull(profilePasswordTokenResponse.getRefreshToken());
    assertNull(profilePasswordTokenResponse.getProfile());

    // validate deleted
    TokenEntity tokenEntity = tokenRepository.findById(tokenId).orElse(null);
    assertNotNull(tokenEntity);
    assertNotNull(tokenEntity.getDeletedDate());
  }

  @Test
  public void testSetTokensAsDeletedByProfileId() {
    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    ProfileEntity profileEntity = TestData.getProfileEntities().getLast();
    TokenEntity tokenEntity1 = TestData.getTokenEntity(101, platformEntity, profileEntity);
    TokenEntity tokenEntity2 = TestData.getTokenEntity(102, platformEntity, profileEntity);
    tokenRepository.save(tokenEntity1);
    tokenRepository.save(tokenEntity2);

    int updated = tokenService.setTokenDeletedDateByProfileId(profileEntity.getId());
    assertEquals(2, updated);
  }
}
