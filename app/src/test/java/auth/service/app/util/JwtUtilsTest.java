package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import auth.service.BaseTest;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.model.dto.AppUserDto;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.token.AuthToken;
import helper.TestData;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

public class JwtUtilsTest extends BaseTest {

  private static final String INVALID_TOKEN = "invalid.token.string";
  private static final AppUserDto appUserDto = new AppUserDto();

  @BeforeAll
  public static void setUp() {
    AppUserEntity appUserEntity = TestData.getAppUserEntities().getFirst();
    BeanUtils.copyProperties(appUserEntity, appUserDto, "password", "addresses");
    appUserDto.setRoles(new ArrayList<>());
  }

  @Test
  public void testEncodeEmailAddress() {
    String encodedEmail = JwtUtils.encodeEmailAddress(APP_USER_EMAIL);
    assertNotNull(encodedEmail);
  }

  @Test
  public void testDecodeEmailAddress() {
    String encodedEmail = JwtUtils.encodeEmailAddress(APP_USER_EMAIL);
    String decodedEmail = JwtUtils.decodeEmailAddress(encodedEmail);
    assertEquals(APP_USER_EMAIL, decodedEmail);
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
    String token = JwtUtils.encodeAuthCredentials(APP_ID, appUserDto, 3600000L);
    assertNotNull(token);
  }

  @Test
  void testDecodeAuthCredentials() {
    String token = JwtUtils.encodeAuthCredentials(APP_ID, appUserDto, 3600000L);
    Map<String, AuthToken> result = JwtUtils.decodeAuthCredentials(token);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey(appUserDto.getEmail()));
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
    String token = JwtUtils.encodeAuthCredentials(APP_ID, appUserDto, 1000);
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
