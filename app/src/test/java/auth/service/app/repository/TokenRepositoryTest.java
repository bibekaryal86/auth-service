package auth.service.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import auth.service.BaseTest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.TokenEntity;
import helper.TestData;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class TokenRepositoryTest extends BaseTest {

  @Autowired private TokenRepository tokenRepository;

  @BeforeAll
  static void setUp(@Autowired TokenRepository tokenRepository) {
    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setAccessToken("some-access-token");
    tokenEntity.setRefreshToken("some-refresh-token");
    tokenEntity.setIpAddress("some-ip-address");

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
  public void testFindByAccessToken() {
    Optional<TokenEntity> tokenEntityOptional =
        tokenRepository.findByAccessToken("some-access-token");

    assertTrue(tokenEntityOptional.isPresent());
    assertEquals("some-refresh-token", tokenEntityOptional.get().getRefreshToken());
  }

  @Test
  public void testFindByRefreshToken() {
    Optional<TokenEntity> tokenEntityOptional =
        tokenRepository.findByRefreshToken("some-refresh-token");

    assertTrue(tokenEntityOptional.isPresent());
    assertEquals("some-access-token", tokenEntityOptional.get().getAccessToken());
  }

  @Test
  public void testUniqueConstraints_accessRefreshToken() {
    TokenEntity tokenEntity = tokenRepository.findByAccessToken("some-access-token").orElse(null);

    if (tokenEntity == null) {
      fail("Token Entity Not Found...");
    }

    TokenEntity tokenEntityOne = new TokenEntity();
    BeanUtils.copyProperties(tokenEntity, tokenEntityOne, "id");

    // throws exception for same access token
    assertThrows(DataIntegrityViolationException.class, () -> tokenRepository.save(tokenEntityOne));

    tokenEntityOne.setAccessToken("some-access-token-1");
    // throws exception for same refresh token
    assertThrows(DataIntegrityViolationException.class, () -> tokenRepository.save(tokenEntityOne));

    tokenEntityOne.setRefreshToken("some-refresh-token-2");
    // does not throw exception for different access and refresh token
    tokenRepository.save(tokenEntityOne);
  }
}
