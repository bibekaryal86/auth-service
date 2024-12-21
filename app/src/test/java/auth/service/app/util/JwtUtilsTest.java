package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.model.dto.ProfileDto;
import auth.service.app.model.dto.StatusTypeDto;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.StatusTypeEntity;
import auth.service.app.model.token.AuthToken;
import helper.TestData;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

public class JwtUtilsTest extends BaseTest {

  private static final String INVALID_TOKEN = "invalid.token.string";
  private static final ProfileDto profileDto = new ProfileDto();
  private static PlatformEntity platformEntity;

  @BeforeAll
  public static void setUp() {
    ProfileEntity profileEntity = TestData.getProfileEntities().getFirst();
    BeanUtils.copyProperties(profileEntity, profileDto, "password", "status", "addresses");
    StatusTypeEntity statusTypeEntity = TestData.getStatusTypeEntities().getFirst();
    StatusTypeDto statusTypeDto = new StatusTypeDto();
    BeanUtils.copyProperties(statusTypeEntity, statusTypeDto);
    profileDto.setStatus(statusTypeDto);
    profileDto.setPlatformRolesMap(Collections.emptyMap());
    platformEntity = TestData.getPlatformEntities().getFirst();
  }

  @Test
  public void testEncodeEmailAddress() {
    String encodedEmail = JwtUtils.encodeEmailAddress(EMAIL);
    assertNotNull(encodedEmail);
  }

  @Test
  public void testDecodeEmailAddress() {
    String encodedEmail = JwtUtils.encodeEmailAddress(EMAIL);
    String decodedEmail = JwtUtils.decodeEmailAddress(encodedEmail);
    assertEquals(EMAIL, decodedEmail);
  }

  @Test
  public void testDecodeEmailAddress_InvalidToken() {
    JwtInvalidException exception =
        assertThrows(
            JwtInvalidException.class,
            () -> {
              JwtUtils.decodeEmailAddress(INVALID_TOKEN);
            });
    assertEquals("Invalid Email Credentials", exception.getMessage());
  }

  @Test
  public void testDecodeEmailAddressNoException() {
    String decodedEmail = JwtUtils.decodeEmailAddressNoException(INVALID_TOKEN);
    assertEquals(INVALID_TOKEN, decodedEmail);
  }

  @Test
  void testEncodeAuthCredentials() {
    String token = JwtUtils.encodeAuthCredentials(platformEntity, profileDto, 3600000L);
    assertNotNull(token);
  }

  @Test
  void testDecodeAuthCredentials() {
    String token = JwtUtils.encodeAuthCredentials(platformEntity, profileDto, 3600000L);
    Map<String, AuthToken> result = JwtUtils.decodeAuthCredentials(token);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey(profileDto.getEmail()));
  }

  @Test
  public void testDecodeAuthCredentials_InvalidToken() {
    JwtInvalidException exception =
        assertThrows(
            JwtInvalidException.class,
            () -> {
              JwtUtils.decodeAuthCredentials(INVALID_TOKEN);
            });
    assertEquals("Invalid Auth Credentials", exception.getMessage());
  }

  @Test
  void testDecodeAuthCredentials_ExpiredToken() throws InterruptedException {
    String token = JwtUtils.encodeAuthCredentials(platformEntity, profileDto, 1000);
    Thread.sleep(1000);
    JwtInvalidException exception =
        assertThrows(
            JwtInvalidException.class,
            () -> {
              JwtUtils.decodeAuthCredentials(token);
            });
    assertEquals("Expired Auth Credentials", exception.getMessage());
  }
}
