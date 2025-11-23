package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import helper.TestData;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TokenRepositoryTest extends BaseTest {

  @Autowired private TokenRepository tokenRepository;

  @BeforeAll
  static void setUp(@Autowired TokenRepository tokenRepository) {
    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setRefreshToken("some-refresh-token");
    tokenEntity.setCsrfToken("some-csrf-token");
    tokenEntity.setIpAddress("some-ip-address");
    tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(1L));

    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    ProfileEntity profileEntity = TestData.getProfileEntities().getFirst();
    tokenEntity.setPlatform(platformEntity);
    tokenEntity.setProfile(profileEntity);

    tokenRepository.save(tokenEntity);
  }

  @AfterAll
  static void tearDown(@Autowired TokenRepository tokenRepository) {
    tokenRepository.deleteAll();
  }

  @Test
  public void testFindByRefreshToken() {
    Optional<TokenEntity> tokenEntityOptional =
        tokenRepository.findByRefreshToken("some-refresh-token");

    assertTrue(tokenEntityOptional.isPresent());
    assertEquals("some-csrf-token", tokenEntityOptional.get().getCsrfToken());
  }

  @Test
  public void testSetTokensAsDeletedByProfileId() {
    PlatformEntity platformEntity = TestData.getPlatformEntities().getFirst();
    ProfileEntity profileEntity = TestData.getProfileEntities().getLast();
    TokenEntity tokenEntity1 = TestData.getTokenEntity(101, platformEntity, profileEntity);
    TokenEntity tokenEntity2 = TestData.getTokenEntity(102, platformEntity, profileEntity);
    tokenRepository.save(tokenEntity1);
    tokenRepository.save(tokenEntity2);

    int updated = tokenRepository.setTokensAsDeletedByProfileIdTest(profileEntity.getId());
    assertEquals(2, updated);
  }
}
