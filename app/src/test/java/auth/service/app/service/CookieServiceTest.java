package auth.service.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;

import auth.service.BaseTest;
import auth.service.app.util.ConstantUtils;
import auth.service.app.util.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;

public class CookieServiceTest extends BaseTest {
  @Autowired private CookieService cookieService;

  private HttpServletRequest request;

  private final String TOKEN_EMPTY = "";
  private final long AGE_MAX = 3600L;
  private final long AGE_ZERO = 0L;

  @BeforeEach
  void setUp() {
    request = Mockito.mock(HttpServletRequest.class);
  }

  @AfterEach
  void tearDown() {
    reset(request);
  }

  @Test
  void testGetCookieValue_NoCookies_ReturnsNull() {
    Mockito.when(request.getCookies()).thenReturn(null);
    String result = cookieService.getCookieValue(request, ConstantUtils.COOKIE_REFRESH_TOKEN);
    assertNull(result);
  }

  @Test
  void testGetCookieValue_CookieExists_ReturnsValue() {
    Cookie cookie = new Cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, "abc123");
    Mockito.when(request.getCookies()).thenReturn(new Cookie[] {cookie});

    String result = cookieService.getCookieValue(request, ConstantUtils.COOKIE_REFRESH_TOKEN);
    assertEquals("abc123", result);
  }

  @Test
  void testGetCookieValue_CookieDoesNotExist_ReturnsNull() {
    Cookie cookie = new Cookie("other_cookie", "zzz");
    Mockito.when(request.getCookies()).thenReturn(new Cookie[] {cookie});

    String result = cookieService.getCookieValue(request, ConstantUtils.COOKIE_REFRESH_TOKEN);
    assertNull(result);
  }

  @Test
  void testGetCookieValue_MultipleCookies() {
    Cookie[] cookies =
        new Cookie[] {
          new Cookie("x", "1"),
          new Cookie(ConstantUtils.COOKIE_REFRESH_TOKEN, "expectedValue"),
          new Cookie("y", "2")
        };
    Mockito.when(request.getCookies()).thenReturn(cookies);

    String result = cookieService.getCookieValue(request, ConstantUtils.COOKIE_REFRESH_TOKEN);
    assertEquals("expectedValue", result);
  }

  @Test
  void testBuildRefreshCookie() {
    String TOKEN_REFRESH = "some-refresh-token";
    ResponseCookie cookie = cookieService.buildRefreshCookie(TOKEN_REFRESH, AGE_MAX);

    assertEquals(ConstantUtils.COOKIE_REFRESH_TOKEN, cookie.getName());
    assertEquals(TOKEN_REFRESH, cookie.getValue());
    assertEquals(AGE_MAX, cookie.getMaxAge().getSeconds());
    assertEquals("/api/v1/ba_profiles/platform/", cookie.getPath());
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.isSecure());
    assertEquals("Strict", cookie.getSameSite());
  }

  @Test
  void testBuildRefreshCookie_ZeroMaxAge() {
    ResponseCookie cookie = cookieService.buildRefreshCookie(TOKEN_EMPTY, AGE_ZERO);

    assertEquals(ConstantUtils.COOKIE_REFRESH_TOKEN, cookie.getName());
    assertEquals(TOKEN_EMPTY, cookie.getValue());
    assertEquals(AGE_ZERO, cookie.getMaxAge().getSeconds());
  }

  @Test
  void testBuildCsrfCookie() {
    String TOKEN_CSRF = "some-csrf-token";
    ResponseCookie cookie = cookieService.buildCsrfCookie(TOKEN_CSRF, AGE_MAX);

    assertEquals(ConstantUtils.COOKIE_CSRF_TOKEN, cookie.getName());
    assertEquals(TOKEN_CSRF, cookie.getValue());
    assertEquals(AGE_MAX, cookie.getMaxAge().getSeconds());
    assertEquals("/", cookie.getPath());
    assertFalse(cookie.isHttpOnly());
    assertTrue(cookie.isSecure());
    assertEquals("Strict", cookie.getSameSite());
  }

  @Test
  void testBuildCsrfCookie_ZeroMaxAge() {
    ResponseCookie cookie = cookieService.buildCsrfCookie(TOKEN_EMPTY, AGE_ZERO);

    assertEquals(ConstantUtils.COOKIE_CSRF_TOKEN, cookie.getName());
    assertEquals(TOKEN_EMPTY, cookie.getValue());
    assertEquals(AGE_ZERO, cookie.getMaxAge().getSeconds());
  }
}
