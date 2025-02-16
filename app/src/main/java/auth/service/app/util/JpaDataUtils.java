package auth.service.app.util;

import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public class JpaDataUtils {

  /**
   * Returns a Specification to filter records where the given field is null.
   *
   * @param fieldName The name of the field to check for null.
   * @param <T> The type of the entity.
   * @return A Specification to filter records where the field is null.
   */
  public static <T> Specification<T> isFieldNull(final String fieldName) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(fieldName));
  }

  /**
   * Returns a Specification to filter records where an entity matches its Id
   *
   * @param fieldName The name of the complex field to compare.
   * @param id The id of the entity fieldName to compare against.
   * @return A Specification to filter records where the field matches the id of a complex entity.
   */
  public static <T> Specification<T> isFieldEqualToEntityId(final String fieldName, final Long id) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(fieldName).get("id"), id);
  }

  /**
   * Creates a Pageable object for pagination and sorting.
   *
   * @param pageNumber The page number (zero-based).
   * @param perPageSize The number of items per page.
   * @param sortColumn The column to sort by.
   * @param sortDirection The direction of sorting (ASC or DESC).
   * @return A Pageable object.
   * @throws IllegalArgumentException If the page or size is invalid, or if the sort column is
   *     invalid.
   */
  public static <T> Pageable createPageable(
      final int pageNumber,
      final int perPageSize,
      final String sortColumn,
      final Sort.Direction sortDirection) {
    final int validatedPageNumber = Math.max(pageNumber, 0);
    final int validatedPerPageSize = (perPageSize <= 10 || perPageSize > 1000) ? 100 : perPageSize;
    final Sort.Direction direction = Objects.requireNonNullElse(sortDirection, Sort.Direction.ASC);
    return PageRequest.of(
        validatedPageNumber, validatedPerPageSize, Sort.by(direction, sortColumn));
  }
}
