package auth.service.app.util;

import static auth.service.app.util.ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import auth.service.BaseTest;
import auth.service.app.exception.CheckPermissionException;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.exception.ProfileForbiddenException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.token.AuthToken;
import helper.TestData;
import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommonUtilsTest extends BaseTest {

  @Mock private HttpServletRequest request;
  @Mock private SecurityContext securityContext;

  @BeforeEach
  void setUp() {
    reset(request);
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void testGetBaseUrlForLinkInEmail_WithHttp() {
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(8080);
    when(request.getContextPath()).thenReturn("/app");

    String result = CommonUtils.getBaseUrlForLinkInEmail(request);
    assertEquals("http://localhost:8080/app", result);
  }

  @Test
  void testGetBaseUrlForLinkInEmail_WithHttps() {
    when(request.getScheme()).thenReturn("https");
    when(request.getServerName()).thenReturn("example.com");
    when(request.getServerPort()).thenReturn(443);
    when(request.getContextPath()).thenReturn("/secure/app");

    String result = CommonUtils.getBaseUrlForLinkInEmail(request);
    assertEquals("https://example.com/secure/app", result);
  }

  private static Stream<Arguments> provideExceptions() {
    return Stream.of(
        Arguments.of(new ElementNotFoundException("something", "anything"), HttpStatus.NOT_FOUND),
        Arguments.of(new ElementMissingException("something", "anything"), HttpStatus.BAD_REQUEST),
        Arguments.of(new ProfileForbiddenException(), HttpStatus.FORBIDDEN),
        Arguments.of(new ProfileNotValidatedException(), HttpStatus.FORBIDDEN),
        Arguments.of(new ElementNotActiveException("something", "anything"), HttpStatus.FORBIDDEN),
        Arguments.of(new ProfileNotActiveException(), HttpStatus.FORBIDDEN),
        Arguments.of(new ProfileNotAuthorizedException(), HttpStatus.UNAUTHORIZED),
        Arguments.of(new JwtInvalidException("something"), HttpStatus.UNAUTHORIZED),
        Arguments.of(new RuntimeException(), INTERNAL_SERVER_ERROR));
  }

  @ParameterizedTest
  @MethodSource("provideExceptions")
  void testGetHttpStatusForErrorResponse(Exception exception, HttpStatus expectedHttpStatus) {
    HttpStatus httpStatus = CommonUtils.getHttpStatusForErrorResponse(exception);
    assertEquals(expectedHttpStatus, httpStatus);
  }

  @Test
  void testGetHttpStatusAndResponseStatusInfoForSingleResponse_ObjectIsNull() {
    HttpStatus status = CommonUtils.getHttpStatusForSingleResponse(null);
    ResponseMetadata.ResponseStatusInfo responseStatusInfo =
        CommonUtils.getResponseStatusInfoForSingleResponse(null);
    assertEquals(INTERNAL_SERVER_ERROR, status);
    assertNotNull(responseStatusInfo);
    assertEquals(INTERNAL_SERVER_ERROR_MESSAGE, responseStatusInfo.errMsg());
  }

  @Test
  void testGetHttpStatusAndResponseStatusInfoForSingleResponse_ObjectIsNotNull() {
    HttpStatus status = CommonUtils.getHttpStatusForSingleResponse(new Object());
    ResponseMetadata.ResponseStatusInfo responseStatusInfo =
        CommonUtils.getResponseStatusInfoForSingleResponse(new Object());
    assertEquals(OK, status);
    assertNull(responseStatusInfo);
  }

  @Test
  void testGetIpAddress_XForwardedForPresent() {
    String ipAddress = "192.168.1.100";
    when(request.getHeader("X-Forwarded-For")).thenReturn(ipAddress);
    String result = CommonUtils.getIpAddress(request);
    assertEquals(ipAddress, result);
  }

  @Test
  void testGetIpAddress_XForwardedForAbsent() {
    String ipAddress = "192.168.1.100";
    when(request.getRemoteAddr()).thenReturn(ipAddress);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    String result = CommonUtils.getIpAddress(request);
    assertEquals(ipAddress, result);
  }

  @Test
  public void testGetUserAgent_Present() {
    String userAgent = "Mozilla/5.0";
    when(request.getHeader("User-Agent")).thenReturn(userAgent);
    String result = CommonUtils.getUserAgent(request);
    assertEquals(userAgent, result);
  }

  @Test
  public void testGetUserAgent_Absent() {
    when(request.getHeader("User-Agent")).thenReturn(null);
    String result = CommonUtils.getUserAgent(request);
    assertNull(result);
  }

  @Test
  void testEmptyResponseStatusInfo() {
    ResponseMetadata.ResponseStatusInfo result = ResponseMetadata.emptyResponseStatusInfo();
    assertEquals("", result.errMsg());
  }

  @Test
  void testEmptyResponsePageInfo() {
    ResponseMetadata.ResponsePageInfo result = ResponseMetadata.emptyResponsePageInfo();
    assertEquals(0, result.totalItems());
    assertEquals(0, result.totalPages());
    assertEquals(0, result.pageNumber());
    assertEquals(0, result.perPage());
  }

  @Test
  void testEmptyResponseCrudInfo() {
    ResponseMetadata.ResponseCrudInfo result = ResponseMetadata.emptyResponseCrudInfo();
    assertEquals(0, result.insertedRowsCount());
    assertEquals(0, result.updatedRowsCount());
    assertEquals(0, result.deletedRowsCount());
    assertEquals(0, result.restoredRowsCount());
  }

  @Test
  void testEmptyResponseMetadata() {
    ResponseMetadata result = ResponseMetadata.emptyResponseMetadata();
    assertNotNull(result.responseStatusInfo());
    assertNotNull(result.responsePageInfo());
    assertNotNull(result.responseCrudInfo());
    assertEquals("", result.responseStatusInfo().errMsg());
    assertEquals(0, result.responsePageInfo().totalItems());
    assertEquals(0, result.responsePageInfo().totalPages());
    assertEquals(0, result.responsePageInfo().pageNumber());
    assertEquals(0, result.responsePageInfo().perPage());
    assertEquals(0, result.responseCrudInfo().insertedRowsCount());
    assertEquals(0, result.responseCrudInfo().updatedRowsCount());
    assertEquals(0, result.responseCrudInfo().deletedRowsCount());
    assertEquals(0, result.responseCrudInfo().restoredRowsCount());
  }

  private static Stream<Arguments> providePages() {
    return Stream.of(
        Arguments.of(
            new PageImpl<>(Collections.nCopies(50, new Object()), PageRequest.of(9, 100), 5000),
            new ResponseMetadata.ResponsePageInfo(5000, 50, 10, 100)
            ),
        Arguments.of(
            new PageImpl<>(Collections.nCopies(5, new Object()), PageRequest.of(0, 10), 50),
            new ResponseMetadata.ResponsePageInfo(50, 5, 1, 10)));
  }

  @ParameterizedTest
  @MethodSource("providePages")
  void testDefaultResponsePageInfo(Page page, ResponseMetadata.ResponsePageInfo expected) {
    ResponseMetadata.ResponsePageInfo actual = CommonUtils.defaultResponsePageInfo(page);
    assertEquals(expected, actual);
  }

  private static Stream<Arguments> provideCrudInfo() {
    return Stream.of(
        Arguments.of(
            1,
            0,
            0,
            0,
            new ResponseMetadata.ResponseCrudInfo(1, 0, 0, 0)
            ),
        Arguments.of(
            0,
            1,
            0,
            0,
                new ResponseMetadata.ResponseCrudInfo(1, 1, 0, 0)

                ),
        Arguments.of(
            0,
            0,
            1,
            0,
                new ResponseMetadata.ResponseCrudInfo(0, 0, 1, 0)
        ),
        Arguments.of(
            0,
            0,
            0,
            1,
                new ResponseMetadata.ResponseCrudInfo(0, 0, 0, 1)

                ));
  }

  @ParameterizedTest
  @MethodSource("provideCrudInfo")
  void testDefaultResponseCrudInfo(
      int inserted, int updated, int deleted, int restored, ResponseMetadata.ResponseCrudInfo expected) {
    ResponseMetadata.ResponseCrudInfo actual =
        CommonUtils.defaultResponseCrudInfo(inserted, updated, deleted, restored);
    assertEquals(expected, actual);
  }

  @Test
  void testGetAuthentication() {
    AuthToken expected = TestData.getAuthToken();
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, expected, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    AuthToken actual = CommonUtils.getAuthentication();
    assertEquals(expected, actual);
  }

  @Test
  void testGetAuthentication_NullAuthentication() {
    when(securityContext.getAuthentication()).thenReturn(null);
    assertThrows(
        CheckPermissionException.class,
        CommonUtils::getAuthentication,
        "Profile not authenticated...");
  }

  @Test
  void testGetAuthentication_NullPrincipal() {
    AuthToken expected = TestData.getAuthToken();
    Authentication authentication =
        new TestingAuthenticationToken(null, expected, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);
    assertThrows(
        CheckPermissionException.class,
        CommonUtils::getAuthentication,
        "Profile not authenticated...");
  }

  @Test
  void testGetAuthentication_IsNotAuthenticated() {
    AuthToken expected = TestData.getAuthToken();
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, expected, Collections.emptyList());
    authentication.setAuthenticated(false);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    assertThrows(
        CheckPermissionException.class,
        CommonUtils::getAuthentication,
        "Profile not authenticated...");
  }

  @Test
  void testGetAuthentication_NullCredentials() {
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, null, Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);
    assertThrows(
        CheckPermissionException.class,
        CommonUtils::getAuthentication,
        "Profile not authorized...");
  }

  @Test
  void testGetAuthentication_InvalidCredentials() {
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, "something-else", Collections.emptyList());
    when(securityContext.getAuthentication()).thenReturn(authentication);
    assertThrows(
        CheckPermissionException.class,
        CommonUtils::getAuthentication,
        "Profile not authorized...");
  }
}
