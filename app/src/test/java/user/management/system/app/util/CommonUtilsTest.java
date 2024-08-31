package user.management.system.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static user.management.system.app.util.ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import user.management.system.BaseTest;
import user.management.system.app.exception.ElementMissingException;
import user.management.system.app.exception.ElementNotActiveException;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.exception.JwtInvalidException;
import user.management.system.app.exception.UserForbiddenException;
import user.management.system.app.exception.UserNotActiveException;
import user.management.system.app.exception.UserNotAuthorizedException;
import user.management.system.app.exception.UserNotValidatedException;
import user.management.system.app.model.dto.ResponseStatusInfo;

@ExtendWith(MockitoExtension.class)
public class CommonUtilsTest extends BaseTest {

  @Mock private HttpServletRequest request;

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
        Arguments.of(new UserForbiddenException(), HttpStatus.FORBIDDEN),
        Arguments.of(new UserNotValidatedException(), HttpStatus.FORBIDDEN),
        Arguments.of(new ElementNotActiveException("something", "anything"), HttpStatus.FORBIDDEN),
        Arguments.of(new UserNotActiveException(), HttpStatus.FORBIDDEN),
        Arguments.of(new UserNotAuthorizedException(), HttpStatus.UNAUTHORIZED),
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
    ResponseStatusInfo responseStatusInfo =
        CommonUtils.getResponseStatusInfoForSingleResponse(null);
    assertEquals(INTERNAL_SERVER_ERROR, status);
    assertNotNull(responseStatusInfo);
    assertEquals(INTERNAL_SERVER_ERROR_MESSAGE, responseStatusInfo.getErrMsg());
  }

  @Test
  void testGetHttpStatusAndResponseStatusInfoForSingleResponse_ObjectIsNotNull() {
    HttpStatus status = CommonUtils.getHttpStatusForSingleResponse(new Object());
    ResponseStatusInfo responseStatusInfo =
        CommonUtils.getResponseStatusInfoForSingleResponse(new Object());
    assertEquals(OK, status);
    assertNull(responseStatusInfo);
  }

  @Test
  void testConvertResponseStatusInfoToJson() {
    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().message("Success").errMsg("No errors").build();

    String result = CommonUtils.convertResponseStatusInfoToJson(responseStatusInfo);

    assertEquals("{\"message\":\"Success\",\"errMsg\":\"No errors\"}", result);
  }

  @Test
  void testConvertResponseStatusInfoToJson_MessageIsNull() {
    ResponseStatusInfo responseStatusInfo =
        ResponseStatusInfo.builder().errMsg("No errors").build();
    String result = CommonUtils.convertResponseStatusInfoToJson(responseStatusInfo);
    assertEquals("{\"message\":\"\",\"errMsg\":\"No errors\"}", result);
  }
}
