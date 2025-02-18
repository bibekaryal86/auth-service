package auth.service.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import auth.service.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class JpaDataUtilsTest extends BaseTest {

  @Test
  void testCreatePageable_validInput() {
    int pageNumber = 2;
    int perPageSize = 50;
    String sortColumn = "name";
    Sort.Direction sortDirection = Sort.Direction.DESC;

    Pageable pageable =
        JpaDataUtils.createPageable(pageNumber, perPageSize, sortColumn, sortDirection);

    assertEquals(2, pageable.getPageNumber());
    assertEquals(50, pageable.getPageSize());
    assertEquals(Sort.by(sortDirection, sortColumn), pageable.getSort());
  }

  @Test
  void testCreatePageable_invalidPageNumber() {
    int pageNumber = -1;
    int perPageSize = 50;
    String sortColumn = "name";
    Sort.Direction sortDirection = Sort.Direction.DESC;

    Pageable pageable =
        JpaDataUtils.createPageable(pageNumber, perPageSize, sortColumn, sortDirection);

    assertEquals(0, pageable.getPageNumber());
  }

  @Test
  void testCreatePageable_invalidPerPageSize() {
    int pageNumber = 2;
    int perPageSize = 5;
    String sortColumn = "name";
    Sort.Direction sortDirection = Sort.Direction.DESC;

    Pageable pageable =
        JpaDataUtils.createPageable(pageNumber, perPageSize, sortColumn, sortDirection);

    // Then
    assertEquals(100, pageable.getPageSize());
  }

  @Test
  void testCreatePageable_invalidPerPageSize_large() {
    int pageNumber = 2;
    int perPageSize = 2000;
    String sortColumn = "name";
    Sort.Direction sortDirection = Sort.Direction.DESC;

    Pageable pageable =
        JpaDataUtils.createPageable(pageNumber, perPageSize, sortColumn, sortDirection);

    assertEquals(100, pageable.getPageSize());
  }

  @Test
  void testCreatePageable_nullSortDirection() {
    int pageNumber = 2;
    int perPageSize = 50;
    String sortColumn = "name";
    Sort.Direction sortDirection = null;

    Pageable pageable =
        JpaDataUtils.createPageable(pageNumber, perPageSize, sortColumn, sortDirection);

    assertEquals(Sort.by(Sort.Direction.ASC, sortColumn), pageable.getSort());
  }

  @Test
  void testCreatePageable_nullSortColumn() {
    int pageNumber = 2;
    int perPageSize = 50;
    String sortColumn = null;
    Sort.Direction sortDirection = Sort.Direction.DESC;

    assertThrows(
        IllegalArgumentException.class,
        () -> JpaDataUtils.createPageable(pageNumber, perPageSize, sortColumn, sortDirection));
  }
}
