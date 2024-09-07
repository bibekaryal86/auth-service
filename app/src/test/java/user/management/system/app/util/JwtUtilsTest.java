package user.management.system.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import helper.TestData;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import user.management.system.BaseTest;
import user.management.system.app.exception.JwtInvalidException;
import user.management.system.app.model.dto.AppUserDto;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.token.AuthToken;

public class JwtUtilsTest extends BaseTest {

  private static final String TEST_APP_ID = "app-1";
  private static final String TEST_EMAIL = "firstlast@one.com";
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
    String encodedEmail = JwtUtils.encodeEmailAddress(TEST_EMAIL);
    assertNotNull(encodedEmail);
  }

  @Test
  public void testDecodeEmailAddress() {
    String encodedEmail = JwtUtils.encodeEmailAddress(TEST_EMAIL);
    String decodedEmail = JwtUtils.decodeEmailAddress(encodedEmail);
    assertEquals(TEST_EMAIL, decodedEmail);
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
    String token = JwtUtils.encodeAuthCredentials(TEST_APP_ID, appUserDto, 3600000L);
    assertNotNull(token);
  }

  @Test
  void testDecodeAuthCredentials() {
    String token = JwtUtils.encodeAuthCredentials(TEST_APP_ID, appUserDto, 3600000L);
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
    String token = JwtUtils.encodeAuthCredentials(TEST_APP_ID, appUserDto, 1000);
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
