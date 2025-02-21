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
  void testGetQueryPageable_defaultSortColumn() {
    RequestMetadata requestMetadata = new RequestMetadata();
    requestMetadata.setPageNumber(1);
    requestMetadata.setPerPage(10);
    String defaultSortColumn = "id";

    Pageable result = JpaDataUtils.getQueryPageable(requestMetadata, defaultSortColumn);

    assertNotNull(result);
    assertEquals(1, result.getPageNumber());
    assertEquals(10, result.getPageSize());
    assertEquals(defaultSortColumn, result.getSort().iterator().next().getProperty());
    assertEquals(Sort.Direction.ASC, result.getSort().iterator().next().getDirection());
  }

  @Test
  void testGetQueryPageable_customSortColumn() {
    RequestMetadata requestMetadata = new RequestMetadata();
    requestMetadata.setPageNumber(2);
    requestMetadata.setPerPage(20);
    requestMetadata.setSortColumn("name");
    requestMetadata.setSortDirection(Sort.Direction.DESC);
    String defaultSortColumn = "id";

    Pageable result = JpaDataUtils.getQueryPageable(requestMetadata, defaultSortColumn);

    assertNotNull(result);
    assertEquals(2, result.getPageNumber());
    assertEquals(20, result.getPageSize());
    assertEquals(requestMetadata.getSortColumn(), result.getSort().iterator().next().getProperty());
    assertEquals(
        requestMetadata.getSortDirection(), result.getSort().iterator().next().getDirection());
  }

  @Test
  void testGetQueryPageable_emptySortColumn() {
    RequestMetadata requestMetadata = new RequestMetadata();
    requestMetadata.setPageNumber(1);
    requestMetadata.setPerPage(100);
    requestMetadata.setSortColumn("");
    String defaultSortColumn = "id";

    Pageable result = JpaDataUtils.getQueryPageable(requestMetadata, defaultSortColumn);

    assertNotNull(result);
    assertEquals(1, result.getPageNumber());
    assertEquals(100, result.getPageSize());
    assertEquals(defaultSortColumn, result.getSort().iterator().next().getProperty());
  }

  @Test
  void testGetQueryPageable_NullSortColumn() {
    RequestMetadata requestMetadata = new RequestMetadata();
    requestMetadata.setPageNumber(1);
    requestMetadata.setPerPage(100);
    requestMetadata.setSortColumn(null);
    String defaultSortColumn = "id";

    Pageable result = JpaDataUtils.getQueryPageable(requestMetadata, defaultSortColumn);

    assertNotNull(result);
    assertEquals(1, result.getPageNumber());
    assertEquals(100, result.getPageSize());
    assertEquals(defaultSortColumn, result.getSort().iterator().next().getProperty());
  }

  @Test
  void testShouldIncludeDeletedRecords_isIncludeDeletedFalse() {
    RequestMetadata requestMetadata = RequestMetadata.builder().isIncludeDeleted(false).build();
    boolean actual = JpaDataUtils.shouldIncludeDeletedRecords(requestMetadata);
    assertFalse(actual);
  }

  @Test
  void testShouldIncludeDeletedRecords_isIncludeDeletedTrueButNoSuperUser() {
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
  void testShouldIncludeDeletedRecords_isIncludeDeletedTrueWithSuperUser() {
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
  void testShouldIncludeDeletedRecords_isIncludeDeletedFalseWithSuperUser() {
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
