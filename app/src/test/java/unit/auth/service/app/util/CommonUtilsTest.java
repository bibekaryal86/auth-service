package unit.auth.service.app.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import auth.service.app.util.CommonUtils;
import auth.service.app.util.ConstantUtils;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.exception.CheckPermissionException;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CommonUtils Unit Tests")
class CommonUtilsTest {

  @Mock private HttpServletRequest mockRequest;
  @Mock private SecurityContext mockSecurityContext;
  @Mock private Authentication mockAuthentication;
  @Mock private AuthToken mockAuthToken;

  private MockedStatic<CommonUtilities> commonUtilitiesMock;
  private MockedStatic<SecurityContextHolder> securityContextHolderMock;

  @AfterEach
  void tearDown() {
    if (commonUtilitiesMock != null) {
      commonUtilitiesMock.close();
    }
    if (securityContextHolderMock != null) {
      securityContextHolderMock.close();
    }
  }

  @Nested
  @DisplayName("isProduction() tests")
  class IsProductionTests {

    @Test
    @DisplayName("Should return true when environment is production")
    void shouldReturnTrueForProductionEnvironment() {
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock
          .when(() -> CommonUtilities.getSystemEnvProperty(ConstantUtils.SPRING_PROFILES_ACTIVE))
          .thenReturn(ConstantUtils.ENV_PROD);

      Assertions.assertTrue(CommonUtils.isProduction());
    }

    @Test
    @DisplayName("Should return false when environment is not production")
    void shouldReturnFalseForNonProductionEnvironment() {
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock
          .when(() -> CommonUtilities.getSystemEnvProperty(ConstantUtils.SPRING_PROFILES_ACTIVE))
          .thenReturn("dev");

      assertFalse(CommonUtils.isProduction());
    }

    @Test
    @DisplayName("Should return false when environment is empty")
    void shouldReturnFalseForEmptyEnvironment() {
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock
          .when(() -> CommonUtilities.getSystemEnvProperty(ConstantUtils.SPRING_PROFILES_ACTIVE))
          .thenReturn("");

      assertFalse(CommonUtils.isProduction());
    }
  }

  @Nested
  @DisplayName("getBaseUrlForLinkInEmail() tests")
  class GetBaseUrlForLinkInEmailTests {

    @Test
    @DisplayName("Should build URL with default HTTP port (80)")
    void shouldBuildUrlWithDefaultHttpPort() {
      when(mockRequest.getScheme()).thenReturn("http");
      when(mockRequest.getServerName()).thenReturn("example.com");
      when(mockRequest.getServerPort()).thenReturn(80);
      when(mockRequest.getContextPath()).thenReturn("/app");

      String result = CommonUtils.getBaseUrlForLinkInEmail(mockRequest);

      assertEquals("http://example.com/app", result);
    }

    @Test
    @DisplayName("Should build URL with default HTTPS port (443)")
    void shouldBuildUrlWithDefaultHttpsPort() {
      when(mockRequest.getScheme()).thenReturn("https");
      when(mockRequest.getServerName()).thenReturn("example.com");
      when(mockRequest.getServerPort()).thenReturn(443);
      when(mockRequest.getContextPath()).thenReturn("/app");

      String result = CommonUtils.getBaseUrlForLinkInEmail(mockRequest);

      assertEquals("https://example.com/app", result);
    }

    @Test
    @DisplayName("Should build URL with custom HTTP port")
    void shouldBuildUrlWithCustomHttpPort() {
      when(mockRequest.getScheme()).thenReturn("http");
      when(mockRequest.getServerName()).thenReturn("example.com");
      when(mockRequest.getServerPort()).thenReturn(8080);
      when(mockRequest.getContextPath()).thenReturn("/app");

      String result = CommonUtils.getBaseUrlForLinkInEmail(mockRequest);

      assertEquals("http://example.com:8080/app", result);
    }

    @Test
    @DisplayName("Should build URL with custom HTTPS port")
    void shouldBuildUrlWithCustomHttpsPort() {
      when(mockRequest.getScheme()).thenReturn("https");
      when(mockRequest.getServerName()).thenReturn("example.com");
      when(mockRequest.getServerPort()).thenReturn(8443);
      when(mockRequest.getContextPath()).thenReturn("/app");

      String result = CommonUtils.getBaseUrlForLinkInEmail(mockRequest);

      assertEquals("https://example.com:8443/app", result);
    }

    @Test
    @DisplayName("Should build URL with empty context path")
    void shouldBuildUrlWithEmptyContextPath() {
      when(mockRequest.getScheme()).thenReturn("https");
      when(mockRequest.getServerName()).thenReturn("example.com");
      when(mockRequest.getServerPort()).thenReturn(443);
      when(mockRequest.getContextPath()).thenReturn("");

      String result = CommonUtils.getBaseUrlForLinkInEmail(mockRequest);

      assertEquals("https://example.com", result);
    }

    @Test
    @DisplayName("Should handle localhost with custom port")
    void shouldHandleLocalhostWithCustomPort() {
      when(mockRequest.getScheme()).thenReturn("http");
      when(mockRequest.getServerName()).thenReturn("localhost");
      when(mockRequest.getServerPort()).thenReturn(3000);
      when(mockRequest.getContextPath()).thenReturn("");

      String result = CommonUtils.getBaseUrlForLinkInEmail(mockRequest);

      assertEquals("http://localhost:3000", result);
    }
  }

  @Nested
  @DisplayName("getIpAddress() tests")
  class GetIpAddressTests {

    @Test
    @DisplayName("Should return X-Forwarded-For header when present and valid")
    void shouldReturnXForwardedForWhenValid() {
      when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

      String result = CommonUtils.getIpAddress(mockRequest);

      assertEquals("192.168.1.1", result);
    }

    @Test
    @DisplayName("Should return remote address when X-Forwarded-For is null")
    void shouldReturnRemoteAddrWhenXForwardedForIsNull() {
      when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
      when(mockRequest.getRemoteAddr()).thenReturn("10.0.0.1");

      String result = CommonUtils.getIpAddress(mockRequest);

      assertEquals("10.0.0.1", result);
    }

    @Test
    @DisplayName("Should return remote address when X-Forwarded-For is empty")
    void shouldReturnRemoteAddrWhenXForwardedForIsEmpty() {
      when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("");
      when(mockRequest.getRemoteAddr()).thenReturn("10.0.0.1");

      String result = CommonUtils.getIpAddress(mockRequest);

      assertEquals("10.0.0.1", result);
    }

    @Test
    @DisplayName("Should return remote address when X-Forwarded-For is 'unknown'")
    void shouldReturnRemoteAddrWhenXForwardedForIsUnknown() {
      when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("unknown");
      when(mockRequest.getRemoteAddr()).thenReturn("10.0.0.1");

      String result = CommonUtils.getIpAddress(mockRequest);

      assertEquals("10.0.0.1", result);
    }

    @Test
    @DisplayName(
        "Should return remote address when X-Forwarded-For is 'UNKNOWN' (case insensitive)")
    void shouldReturnRemoteAddrWhenXForwardedForIsUnknownUpperCase() {
      when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("UNKNOWN");
      when(mockRequest.getRemoteAddr()).thenReturn("10.0.0.1");

      String result = CommonUtils.getIpAddress(mockRequest);

      assertEquals("10.0.0.1", result);
    }

    @Test
    @DisplayName("Should handle multiple IPs in X-Forwarded-For")
    void shouldHandleMultipleIpsInXForwardedFor() {
      when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

      String result = CommonUtils.getIpAddress(mockRequest);

      assertEquals("192.168.1.1, 10.0.0.1", result);
    }
  }

  @Nested
  @DisplayName("getUserAgent() tests")
  class GetUserAgentTests {

    @Test
    @DisplayName("Should return User-Agent header when present")
    void shouldReturnUserAgentWhenPresent() {
      when(mockRequest.getHeader("User-Agent"))
          .thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

      String result = CommonUtils.getUserAgent(mockRequest);

      assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64)", result);
    }

    @Test
    @DisplayName("Should return null when User-Agent header is not present")
    void shouldReturnNullWhenUserAgentNotPresent() {
      when(mockRequest.getHeader("User-Agent")).thenReturn(null);

      String result = CommonUtils.getUserAgent(mockRequest);

      assertNull(result);
    }

    @Test
    @DisplayName("Should return empty string when User-Agent is empty")
    void shouldReturnEmptyStringWhenUserAgentIsEmpty() {
      when(mockRequest.getHeader("User-Agent")).thenReturn("");

      String result = CommonUtils.getUserAgent(mockRequest);

      assertEquals("", result);
    }
  }

  @Nested
  @DisplayName("defaultResponseCrudInfo() tests")
  class DefaultResponseCrudInfoTests {

    @Test
    @DisplayName("Should create ResponseCrudInfo with all zero values")
    void shouldCreateResponseCrudInfoWithZeroValues() {
      ResponseMetadata.ResponseCrudInfo result = CommonUtils.defaultResponseCrudInfo(0, 0, 0, 0);

      assertNotNull(result);
      assertEquals(0, result.insertedRowsCount());
      assertEquals(0, result.updatedRowsCount());
      assertEquals(0, result.deletedRowsCount());
      assertEquals(0, result.restoredRowsCount());
    }

    @Test
    @DisplayName("Should create ResponseCrudInfo with positive values")
    void shouldCreateResponseCrudInfoWithPositiveValues() {
      ResponseMetadata.ResponseCrudInfo result = CommonUtils.defaultResponseCrudInfo(5, 3, 2, 1);

      assertNotNull(result);
      assertEquals(5, result.insertedRowsCount());
      assertEquals(3, result.updatedRowsCount());
      assertEquals(2, result.deletedRowsCount());
      assertEquals(1, result.restoredRowsCount());
    }

    @Test
    @DisplayName("Should create ResponseCrudInfo with negative values")
    void shouldCreateResponseCrudInfoWithNegativeValues() {
      ResponseMetadata.ResponseCrudInfo result =
          CommonUtils.defaultResponseCrudInfo(-1, -2, -3, -4);

      assertNotNull(result);
      assertEquals(-1, result.insertedRowsCount());
      assertEquals(-2, result.updatedRowsCount());
      assertEquals(-3, result.deletedRowsCount());
      assertEquals(-4, result.restoredRowsCount());
    }
  }

  @Nested
  @DisplayName("getAuthentication() tests")
  class GetAuthenticationTests {

    @BeforeEach
    void setUp() {
      securityContextHolderMock = mockStatic(SecurityContextHolder.class);
      securityContextHolderMock
          .when(SecurityContextHolder::getContext)
          .thenReturn(mockSecurityContext);
    }

    @Test
    @DisplayName("Should return AuthToken when authentication is valid")
    void shouldReturnAuthTokenWhenAuthenticationIsValid() {
      when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
      when(mockAuthentication.getPrincipal()).thenReturn(new Object());
      when(mockAuthentication.isAuthenticated()).thenReturn(true);
      when(mockAuthentication.getCredentials()).thenReturn(mockAuthToken);

      AuthToken result = CommonUtils.getAuthentication();

      assertNotNull(result);
      assertEquals(mockAuthToken, result);
    }

    @Test
    @DisplayName("Should throw exception when authentication is null")
    void shouldThrowExceptionWhenAuthenticationIsNull() {
      when(mockSecurityContext.getAuthentication()).thenReturn(null);

      CheckPermissionException exception =
          assertThrows(CheckPermissionException.class, CommonUtils::getAuthentication);

      assertEquals("Permission Denied: Profile not authenticated...", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when principal is null")
    void shouldThrowExceptionWhenPrincipalIsNull() {
      when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
      when(mockAuthentication.getPrincipal()).thenReturn(null);

      CheckPermissionException exception =
          assertThrows(CheckPermissionException.class, CommonUtils::getAuthentication);

      assertEquals("Permission Denied: Profile not authenticated...", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when not authenticated")
    void shouldThrowExceptionWhenNotAuthenticated() {
      when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
      when(mockAuthentication.getPrincipal()).thenReturn(new Object());
      when(mockAuthentication.isAuthenticated()).thenReturn(false);

      CheckPermissionException exception =
          assertThrows(CheckPermissionException.class, CommonUtils::getAuthentication);

      assertEquals("Permission Denied: Profile not authenticated...", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when credentials is null")
    void shouldThrowExceptionWhenCredentialsIsNull() {
      when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
      when(mockAuthentication.getPrincipal()).thenReturn(new Object());
      when(mockAuthentication.isAuthenticated()).thenReturn(true);
      when(mockAuthentication.getCredentials()).thenReturn(null);

      CheckPermissionException exception =
          assertThrows(CheckPermissionException.class, CommonUtils::getAuthentication);

      assertEquals("Permission Denied: Profile not authorized...", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when credentials is not AuthToken")
    void shouldThrowExceptionWhenCredentialsIsNotAuthToken() {
      when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
      when(mockAuthentication.getPrincipal()).thenReturn(new Object());
      when(mockAuthentication.isAuthenticated()).thenReturn(true);
      when(mockAuthentication.getCredentials()).thenReturn("not-an-auth-token");

      CheckPermissionException exception =
          assertThrows(CheckPermissionException.class, CommonUtils::getAuthentication);

      assertEquals("Permission Denied: Profile not authorized...", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("isSuperUser() tests")
  class IsSuperUserTests {

    @Test
    @DisplayName("Should return true when user is super user")
    void shouldReturnTrueWhenUserIsSuperUser() {
      when(mockAuthToken.getIsSuperUser()).thenReturn(true);

      assertTrue(CommonUtils.isSuperUser(mockAuthToken));
    }

    @Test
    @DisplayName("Should return false when user is not super user")
    void shouldReturnFalseWhenUserIsNotSuperUser() {
      when(mockAuthToken.getIsSuperUser()).thenReturn(false);

      assertFalse(CommonUtils.isSuperUser(mockAuthToken));
    }

    @Test
    @DisplayName("Should return false when isSuperUser is null")
    void shouldReturnFalseWhenIsSuperUserIsNull() {
      when(mockAuthToken.getIsSuperUser()).thenReturn(null);

      assertFalse(CommonUtils.isSuperUser(mockAuthToken));
    }
  }

  @Nested
  @DisplayName("getValidId() tests")
  class GetValidIdTests {

    @Test
    @DisplayName("Should return valid Long for positive number string")
    void shouldReturnValidLongForPositiveNumber() {
      Long result = CommonUtils.getValidId("123");

      assertEquals(123L, result);
    }

    @Test
    @DisplayName("Should return valid Long for positive number with whitespace")
    void shouldReturnValidLongForPositiveNumberWithWhitespace() {
      Long result = CommonUtils.getValidId("  456  ");

      assertEquals(456L, result);
    }

    @Test
    @DisplayName("Should return null for zero")
    void shouldReturnNullForZero() {
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock.when(() -> CommonUtilities.isEmpty("0")).thenReturn(false);

      Long result = CommonUtils.getValidId("0");

      assertNull(result);
    }

    @Test
    @DisplayName("Should return null for negative number")
    void shouldReturnNullForNegativeNumber() {
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock.when(() -> CommonUtilities.isEmpty("-5")).thenReturn(false);

      Long result = CommonUtils.getValidId("-5");

      assertNull(result);
    }

    @Test
    @DisplayName("Should return null for empty string")
    void shouldReturnNullForEmptyString() {
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock.when(() -> CommonUtilities.isEmpty("")).thenReturn(true);

      Long result = CommonUtils.getValidId("");

      assertNull(result);
    }

    @Test
    @DisplayName("Should return null for non-numeric string")
    void shouldReturnNullForNonNumericString() {
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock.when(() -> CommonUtilities.isEmpty("abc")).thenReturn(false);

      Long result = CommonUtils.getValidId("abc");

      assertNull(result);
    }

    @Test
    @DisplayName("Should return null for decimal number")
    void shouldReturnNullForDecimalNumber() {
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock.when(() -> CommonUtilities.isEmpty("123.45")).thenReturn(false);

      Long result = CommonUtils.getValidId("123.45");

      assertNull(result);
    }

    @Test
    @DisplayName("Should return valid Long for very large number")
    void shouldReturnValidLongForVeryLargeNumber() {
      String largeNumber = String.valueOf(Long.MAX_VALUE);
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock.when(() -> CommonUtilities.isEmpty(largeNumber)).thenReturn(false);

      Long result = CommonUtils.getValidId(largeNumber);

      assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    @DisplayName("Should return null for number too large for Long")
    void shouldReturnNullForNumberTooLargeForLong() {
      String tooLarge = "9223372036854775808";
      commonUtilitiesMock = mockStatic(CommonUtilities.class);
      commonUtilitiesMock.when(() -> CommonUtilities.isEmpty(tooLarge)).thenReturn(false);

      Long result = CommonUtils.getValidId(tooLarge);

      assertNull(result);
    }
  }
}
