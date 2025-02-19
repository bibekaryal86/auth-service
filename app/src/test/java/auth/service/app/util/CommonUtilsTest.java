package auth.service.app.util;

import static auth.service.app.util.ConstantUtils.INTERNAL_SERVER_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import auth.service.BaseTest;
import auth.service.app.exception.ElementMissingException;
import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.exception.JwtInvalidException;
import auth.service.app.exception.ProfileForbiddenException;
import auth.service.app.exception.ProfileNotActiveException;
import auth.service.app.exception.ProfileNotAuthorizedException;
import auth.service.app.exception.ProfileNotValidatedException;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.ResponseCrudInfo;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponsePageInfo;
import auth.service.app.model.dto.ResponseStatusInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

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
    ResponseStatusInfo result = CommonUtils.emptyResponseStatusInfo();
    assertEquals("", result.getErrMsg());
  }

  @Test
  void testEmptyResponsePageInfo() {
    ResponsePageInfo result = CommonUtils.emptyResponsePageInfo();
    assertEquals(0, result.getTotalItems());
    assertEquals(0, result.getTotalPages());
    assertEquals(0, result.getPageNumber());
    assertEquals(0, result.getPerPage());
  }

  @Test
  void testEmptyResponseCrudInfo() {
    ResponseCrudInfo result = CommonUtils.emptyResponseCrudInfo();
    assertEquals(0, result.getInsertedRowsCount());
    assertEquals(0, result.getUpdatedRowsCount());
    assertEquals(0, result.getDeletedRowsCount());
    assertEquals(0, result.getRestoredRowsCount());
  }

  @Test
  void testEmptyResponseMetadata() {
    ResponseMetadata result = CommonUtils.emptyResponseMetadata();
    assertNotNull(result.getResponseStatusInfo());
    assertNotNull(result.getResponsePageInfo());
    assertNotNull(result.getResponseCrudInfo());
    assertEquals("", result.getResponseStatusInfo().getErrMsg());
    assertEquals(0, result.getResponsePageInfo().getTotalItems());
    assertEquals(0, result.getResponsePageInfo().getTotalPages());
    assertEquals(0, result.getResponsePageInfo().getPageNumber());
    assertEquals(0, result.getResponsePageInfo().getPerPage());
    assertEquals(0, result.getResponseCrudInfo().getInsertedRowsCount());
    assertEquals(0, result.getResponseCrudInfo().getUpdatedRowsCount());
    assertEquals(0, result.getResponseCrudInfo().getDeletedRowsCount());
    assertEquals(0, result.getResponseCrudInfo().getRestoredRowsCount());
  }

  private static Stream<Arguments> providePages() {
    return Stream.of(
            Arguments.of(
                    new PageImpl<>(Collections.nCopies(50, new Object()), PageRequest.of(9, 100), 5000),
                    ResponsePageInfo.builder()
                            .totalItems(5000)
                            .totalPages(50)
                            .pageNumber(9)
                            .perPage(100)
                            .build()),
        Arguments.of(
            new PageImpl<>(Collections.nCopies(5, new Object()), PageRequest.of(1, 10), 50),
            ResponsePageInfo.builder()
                .totalItems(50)
                .totalPages(5)
                .pageNumber(1)
                .perPage(10)
                .build()));
  }

  @ParameterizedTest
  @MethodSource("providePages")
  void testDefaultResponsePageInfo(Page page, ResponsePageInfo expected) {
    ResponsePageInfo actual = CommonUtils.defaultResponsePageInfo(page);
    assertEquals(expected, actual);
  }

  private static Stream<Arguments> provideRequestMetadataDeleted() {
    return Stream.of(
        Arguments.of(null, false),
        Arguments.of(RequestMetadata.builder().build(), false),
        Arguments.of(RequestMetadata.builder().isIncludeDeleted(true).build(), true),
        Arguments.of(RequestMetadata.builder().sortColumn("someColumn").build(), true),
        Arguments.of(
            RequestMetadata.builder().isIncludeDeleted(true).sortColumn("someColumn").build(),
            true));
  }

  @ParameterizedTest
  @MethodSource("provideRequestMetadataDeleted")
  void testIsRequestMetadataIncluded(RequestMetadata requestMetadata, boolean expected) {
    boolean actual = CommonUtils.isRequestMetadataIncluded(requestMetadata);
    assertEquals(expected, actual);
  }

  private static Stream<Arguments> provideRequestMetadataHistory() {
    return Stream.of(
        Arguments.of(null, false),
        Arguments.of(RequestMetadata.builder().build(), false),
        Arguments.of(RequestMetadata.builder().isIncludeHistory(false).build(), false),
        Arguments.of(RequestMetadata.builder().isIncludeHistory(true).build(), true));
  }

  @ParameterizedTest
  @MethodSource("provideRequestMetadataHistory")
  void testIsHistoryToBeIncluded(RequestMetadata requestMetadata, boolean expected) {
    boolean actual = CommonUtils.isHistoryToBeIncluded(requestMetadata);
    assertEquals(expected, actual);
  }
}
