package unit.auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import auth.service.app.util.ConstantUtils;
import auth.service.app.util.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CookieService Unit Tests")
class CookieServiceTest {

  @Mock private HttpServletRequest mockRequest;

  private CookieService cookieService;

  @BeforeEach
  void setUp() {
    cookieService = new CookieService();
  }

  @Nested
  @DisplayName("getCookieValue() tests")
  class GetCookieValueTests {

    @Test
    @DisplayName("Should return cookie value when cookie exists")
    void shouldReturnCookieValueWhenCookieExists() {
      Cookie cookie1 = new Cookie("sessionId", "abc123");
      Cookie cookie2 = new Cookie("userId", "user456");
      Cookie[] cookies = {cookie1, cookie2};

      when(mockRequest.getCookies()).thenReturn(cookies);

      String result = cookieService.getCookieValue(mockRequest, "sessionId");

      assertEquals("abc123", result);
    }

    @Test
    @DisplayName("Should return null when cookies array is null")
    void shouldReturnNullWhenCookiesArrayIsNull() {
      when(mockRequest.getCookies()).thenReturn(null);

      String result = cookieService.getCookieValue(mockRequest, "sessionId");

      assertNull(result);
    }

    @Test
    @DisplayName("Should return null when cookie with name does not exist")
    void shouldReturnNullWhenCookieDoesNotExist() {
      Cookie cookie1 = new Cookie("sessionId", "abc123");
      Cookie[] cookies = {cookie1};

      when(mockRequest.getCookies()).thenReturn(cookies);

      String result = cookieService.getCookieValue(mockRequest, "nonExistent");

      assertNull(result);
    }

    @Test
    @DisplayName("Should return null when cookies array is empty")
    void shouldReturnNullWhenCookiesArrayIsEmpty() {
      Cookie[] cookies = {};

      when(mockRequest.getCookies()).thenReturn(cookies);

      String result = cookieService.getCookieValue(mockRequest, "sessionId");

      assertNull(result);
    }

    @Test
    @DisplayName("Should return first matching cookie when multiple cookies with same name exist")
    void shouldReturnFirstMatchingCookie() {
      Cookie cookie1 = new Cookie("token", "first");
      Cookie cookie2 = new Cookie("token", "second");
      Cookie[] cookies = {cookie1, cookie2};

      when(mockRequest.getCookies()).thenReturn(cookies);

      String result = cookieService.getCookieValue(mockRequest, "token");

      assertEquals("first", result);
    }

    @Test
    @DisplayName("Should handle cookie with empty value")
    void shouldHandleCookieWithEmptyValue() {
      Cookie cookie = new Cookie("emptyToken", "");
      Cookie[] cookies = {cookie};

      when(mockRequest.getCookies()).thenReturn(cookies);

      String result = cookieService.getCookieValue(mockRequest, "emptyToken");

      assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle cookie with null value")
    void shouldHandleCookieWithNullValue() {
      when(mockRequest.getCookies()).thenReturn(null);

      String result = cookieService.getCookieValue(mockRequest, "nullToken");

      assertNull(result);
    }

    @Test
    @DisplayName("Should be case-sensitive when matching cookie names")
    void shouldBeCaseSensitiveForCookieNames() {
      Cookie cookie = new Cookie("SessionId", "abc123");
      Cookie[] cookies = {cookie};

      when(mockRequest.getCookies()).thenReturn(cookies);

      String result = cookieService.getCookieValue(mockRequest, "sessionId");

      assertNull(result);
    }

    @Test
    @DisplayName("Should handle special characters in cookie value")
    void shouldHandleSpecialCharactersInCookieValue() {
      Cookie cookie = new Cookie("token", "abc-123_xyz.test");
      Cookie[] cookies = {cookie};

      when(mockRequest.getCookies()).thenReturn(cookies);

      String result = cookieService.getCookieValue(mockRequest, "token");

      assertEquals("abc-123_xyz.test", result);
    }

    @Test
    @DisplayName("Should handle multiple cookies and return correct one")
    void shouldHandleMultipleCookiesAndReturnCorrectOne() {
      Cookie cookie1 = new Cookie("cookie1", "value1");
      Cookie cookie2 = new Cookie("cookie2", "value2");
      Cookie cookie3 = new Cookie("cookie3", "value3");
      Cookie[] cookies = {cookie1, cookie2, cookie3};

      when(mockRequest.getCookies()).thenReturn(cookies);

      String result = cookieService.getCookieValue(mockRequest, "cookie2");

      assertEquals("value2", result);
    }
  }

  @Nested
  @DisplayName("buildRefreshCookie() tests")
  class BuildRefreshCookieTests {

    @Test
    @DisplayName("Should build refresh cookie with correct attributes")
    void shouldBuildRefreshCookieWithCorrectAttributes() {
      String token = "refresh-token-123";
      long maxAge = 7200L;

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertNotNull(cookie);
      Assertions.assertEquals(ConstantUtils.COOKIE_REFRESH_TOKEN, cookie.getName());
      assertEquals(token, cookie.getValue());
      assertTrue(cookie.isHttpOnly());
      assertTrue(cookie.isSecure());
      assertEquals("Strict", cookie.getSameSite());
      assertEquals(maxAge, cookie.getMaxAge().getSeconds());
      assertEquals("/api/v1/ba_profiles/platform/", cookie.getPath());
    }

    @Test
    @DisplayName("Should build refresh cookie with empty token")
    void shouldBuildRefreshCookieWithEmptyToken() {
      String token = "";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals("", cookie.getValue());
    }

    @Test
    @DisplayName("Should build refresh cookie with zero maxAge")
    void shouldBuildRefreshCookieWithZeroMaxAge() {
      String token = "token";
      long maxAge = 0L;

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(0L, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Should build refresh cookie with negative maxAge for deletion")
    void shouldBuildRefreshCookieWithNegativeMaxAge() {
      String token = "";
      long maxAge = -1L;

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(-1L, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Should build refresh cookie with long token value")
    void shouldBuildRefreshCookieWithLongToken() {
      String token = "a".repeat(500);
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(token, cookie.getValue());
    }

    @Test
    @DisplayName("Should build refresh cookie with large maxAge value")
    void shouldBuildRefreshCookieWithLargeMaxAge() {
      String token = "token";
      long maxAge = 31536000L; // 1 year in seconds

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(31536000L, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Should always set httpOnly to true for refresh cookie")
    void shouldAlwaysSetHttpOnlyTrueForRefreshCookie() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertTrue(cookie.isHttpOnly(), "Refresh cookie must be httpOnly for security");
    }

    @Test
    @DisplayName("Should always set secure to true for refresh cookie")
    void shouldAlwaysSetSecureTrueForRefreshCookie() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertTrue(cookie.isSecure(), "Refresh cookie must be secure for HTTPS only");
    }

    @Test
    @DisplayName("Should always set SameSite to Strict for refresh cookie")
    void shouldAlwaysSetSameSiteStrictForRefreshCookie() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildRefreshCookie(token, maxAge);

      assertEquals(
          "Strict", cookie.getSameSite(), "Refresh cookie must use Strict SameSite policy");
    }
  }

  @Nested
  @DisplayName("buildCsrfCookie() tests")
  class BuildCsrfCookieTests {

    @Test
    @DisplayName("Should build CSRF cookie with correct attributes")
    void shouldBuildCsrfCookieWithCorrectAttributes() {
      String token = "csrf-token-xyz";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(ConstantUtils.COOKIE_CSRF_TOKEN, cookie.getName());
      assertEquals(token, cookie.getValue());
      assertFalse(cookie.isHttpOnly());
      assertTrue(cookie.isSecure());
      assertEquals("Strict", cookie.getSameSite());
      assertEquals(maxAge, cookie.getMaxAge().getSeconds());
      assertEquals("/", cookie.getPath());
    }

    @Test
    @DisplayName("Should build CSRF cookie with empty token")
    void shouldBuildCsrfCookieWithEmptyToken() {
      String token = "";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals("", cookie.getValue());
    }

    @Test
    @DisplayName("Should build CSRF cookie with zero maxAge")
    void shouldBuildCsrfCookieWithZeroMaxAge() {
      String token = "token";
      long maxAge = 0L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(0L, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Should build CSRF cookie with negative maxAge for deletion")
    void shouldBuildCsrfCookieWithNegativeMaxAge() {
      String token = "";
      long maxAge = -1L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(-1L, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Should build CSRF cookie with long token value")
    void shouldBuildCsrfCookieWithLongToken() {
      String token = "a".repeat(500);
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(token, cookie.getValue());
    }

    @Test
    @DisplayName("Should build CSRF cookie with large maxAge value")
    void shouldBuildCsrfCookieWithLargeMaxAge() {
      String token = "token";
      long maxAge = 31536000L; // 1 year in seconds

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertNotNull(cookie);
      assertEquals(31536000L, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Should set httpOnly to false for CSRF cookie")
    void shouldSetHttpOnlyFalseForCsrfCookie() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertFalse(cookie.isHttpOnly(), "CSRF cookie must be accessible to JavaScript");
    }

    @Test
    @DisplayName("Should always set secure to true for CSRF cookie")
    void shouldAlwaysSetSecureTrueForCsrfCookie() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertTrue(cookie.isSecure(), "CSRF cookie must be secure for HTTPS only");
    }

    @Test
    @DisplayName("Should always set SameSite to Strict for CSRF cookie")
    void shouldAlwaysSetSameSiteStrictForCsrfCookie() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertEquals("Strict", cookie.getSameSite(), "CSRF cookie must use Strict SameSite policy");
    }

    @Test
    @DisplayName("Should set path to root for CSRF cookie")
    void shouldSetPathToRootForCsrfCookie() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie cookie = cookieService.buildCsrfCookie(token, maxAge);

      assertEquals("/", cookie.getPath(), "CSRF cookie should be available site-wide");
    }
  }

  @Nested
  @DisplayName("Cookie security comparisons")
  class CookieSecurityComparisonTests {

    @Test
    @DisplayName("Refresh cookie should be httpOnly but CSRF should not")
    void shouldHaveDifferentHttpOnlySettings() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie refreshCookie = cookieService.buildRefreshCookie(token, maxAge);
      ResponseCookie csrfCookie = cookieService.buildCsrfCookie(token, maxAge);

      assertTrue(refreshCookie.isHttpOnly());
      assertFalse(csrfCookie.isHttpOnly());
    }

    @Test
    @DisplayName("Both cookies should be secure")
    void shouldBothBeSecure() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie refreshCookie = cookieService.buildRefreshCookie(token, maxAge);
      ResponseCookie csrfCookie = cookieService.buildCsrfCookie(token, maxAge);

      assertTrue(refreshCookie.isSecure());
      assertTrue(csrfCookie.isSecure());
    }

    @Test
    @DisplayName("Both cookies should use Strict SameSite policy")
    void shouldBothUseStrictSameSite() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie refreshCookie = cookieService.buildRefreshCookie(token, maxAge);
      ResponseCookie csrfCookie = cookieService.buildCsrfCookie(token, maxAge);

      assertEquals("Strict", refreshCookie.getSameSite());
      assertEquals("Strict", csrfCookie.getSameSite());
    }

    @Test
    @DisplayName("Cookies should have different paths")
    void shouldHaveDifferentPaths() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie refreshCookie = cookieService.buildRefreshCookie(token, maxAge);
      ResponseCookie csrfCookie = cookieService.buildCsrfCookie(token, maxAge);

      assertEquals("/api/v1/ba_profiles/platform/", refreshCookie.getPath());
      assertEquals("/", csrfCookie.getPath());
    }

    @Test
    @DisplayName("Cookies should have different names from constants")
    void shouldHaveDifferentNames() {
      String token = "token";
      long maxAge = 3600L;

      ResponseCookie refreshCookie = cookieService.buildRefreshCookie(token, maxAge);
      ResponseCookie csrfCookie = cookieService.buildCsrfCookie(token, maxAge);

      assertEquals(ConstantUtils.COOKIE_REFRESH_TOKEN, refreshCookie.getName());
      assertEquals(ConstantUtils.COOKIE_CSRF_TOKEN, csrfCookie.getName());
      assertNotEquals(refreshCookie.getName(), csrfCookie.getName());
    }
  }
}
