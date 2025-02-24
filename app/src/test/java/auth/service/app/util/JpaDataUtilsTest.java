package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import auth.service.BaseTest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.token.AuthToken;
import helper.TestData;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class JpaDataUtilsTest extends BaseTest {

  @Mock private SecurityContext securityContext;

  @BeforeEach
  void setUp() {
    reset(securityContext);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void testGetQueryPageable_NullRequestMetadata() {
    Pageable result = JpaDataUtils.getQueryPageable(null);
    assertNotNull(result);
    assertEquals(0, result.getPageNumber());
    assertEquals(100, result.getPageSize());
    assertTrue(result.getSort().isUnsorted());
  }

  @Test
  void testGetQueryPageable_WithResponseMetadata() {
    RequestMetadata requestMetadata = new RequestMetadata();
    requestMetadata.setPageNumber(1);
    requestMetadata.setPerPage(10);
    requestMetadata.setSortColumn("id");
    requestMetadata.setSortDirection(Sort.Direction.DESC);

    Pageable result = JpaDataUtils.getQueryPageable(requestMetadata);

    assertNotNull(result);
    assertEquals(1, result.getPageNumber());
    assertEquals(10, result.getPageSize());
    assertEquals("id", result.getSort().iterator().next().getProperty());
    assertEquals(Sort.Direction.DESC, result.getSort().iterator().next().getDirection());
  }

  @Test
  void testShouldIncludeDeletedRecords_IsIncludeDeletedFalse() {
    RequestMetadata requestMetadata = RequestMetadata.builder().isIncludeDeleted(false).build();
    boolean actual = JpaDataUtils.shouldIncludeDeletedRecords(requestMetadata);
    assertFalse(actual);
  }

  @Test
  void testShouldIncludeDeletedRecords_IsIncludeDeletedTrueButNoSuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setSuperUser(false);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RequestMetadata requestMetadata = RequestMetadata.builder().isIncludeDeleted(true).build();
    boolean actual = JpaDataUtils.shouldIncludeDeletedRecords(requestMetadata);
    assertFalse(actual);
  }

  @Test
  void testShouldIncludeDeletedRecords_IsIncludeDeletedTrueWithSuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RequestMetadata requestMetadata = RequestMetadata.builder().isIncludeDeleted(true).build();
    boolean actual = JpaDataUtils.shouldIncludeDeletedRecords(requestMetadata);
    assertTrue(actual);
  }

  @Test
  void testShouldIncludeDeletedRecords_IsIncludeDeletedFalseWithSuperUser() {
    AuthToken authToken = TestData.getAuthToken();
    authToken.setSuperUser(true);
    Authentication authentication =
        new TestingAuthenticationToken(EMAIL, authToken, Collections.emptyList());
    authentication.setAuthenticated(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    RequestMetadata requestMetadata = RequestMetadata.builder().isIncludeDeleted(false).build();
    boolean actual = JpaDataUtils.shouldIncludeDeletedRecords(requestMetadata);
    assertFalse(actual);
  }
}
