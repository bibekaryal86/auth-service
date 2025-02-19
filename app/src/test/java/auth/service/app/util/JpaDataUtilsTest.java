package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import auth.service.BaseTest;
import auth.service.app.model.dto.RequestMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public class JpaDataUtilsTest extends BaseTest {

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
  void testGetQuerySpecification_DoNotIncludeDeletedRecords() {
    Specification<?> specification =
            JpaDataUtils.getQuerySpecification(
                    RequestMetadata.builder().isIncludeDeleted(false).build());
    assertNotNull(specification);
  }
}
