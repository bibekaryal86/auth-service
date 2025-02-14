package auth.service.app.util;

import auth.service.app.model.enums.RequestEnums;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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
   * Returns a Specification to filter records where the given field is not null.
   *
   * @param fieldName The name of the field to check for non-null.
   * @param <T> The type of the entity.
   * @return A Specification to filter records where the field is not null.
   */
  public static <T> Specification<T> isFieldNotNull(final String fieldName) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(fieldName));
  }

  /**
   * Returns a Specification to filter records where the given field is equal to the specified
   * value.
   *
   * @param fieldName The name of the field to compare.
   * @param value The value to compare against.
   * @param <T> The type of the entity.
   * @return A Specification to filter records where the field is equal to the value.
   */
  public static <T> Specification<T> isFieldEqualTo(final String fieldName, final Object value) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), value);
  }

  /**
   * Returns a Specification to filter records where the given field is not equal to the specified
   * value.
   *
   * @param fieldName The name of the field to compare.
   * @param value The value to compare against.
   * @param <T> The type of the entity.
   * @return A Specification to filter records where the field is not equal to the value.
   */
  public static <T> Specification<T> isFieldNotEqualTo(final String fieldName, final Object value) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(fieldName), value);
  }

  /**
   * Returns a Specification to filter records where the given field is greater than the specified
   * value.
   *
   * @param fieldName The name of the field to compare.
   * @param value The value to compare against.
   * @param <T> The type of the entity.
   * @return A Specification to filter records where the field is greater than the value.
   */
  public static <T, V extends Comparable<? super V>> Specification<T> isFieldGreaterThan(
      final String fieldName, final V value) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThan(root.get(fieldName), value);
  }

  /**
   * Returns a Specification to filter records where the given field is greater than or equal to the
   * specified value.
   *
   * @param fieldName The name of the field to compare.
   * @param value The value to compare against.
   * @param <T> The type of the entity.
   * @return A Specification to filter records where the field is greater than or equal to the
   *     value.
   */
  public static <T, V extends Comparable<? super V>> Specification<T> isFieldGreaterThanOrEqualTo(
      final String fieldName, final V value) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), value);
  }

  /**
   * Returns a Specification to filter records where the given field is less than the specified
   * value.
   *
   * @param fieldName The name of the field to compare.
   * @param value The value to compare against.
   * @param <T> The type of the entity.
   * @return A Specification to filter records where the field is less than the value.
   */
  public static <T, V extends Comparable<? super V>> Specification<T> isFieldLessThan(
      final String fieldName, final V value) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(fieldName), value);
  }

  /**
   * Returns a Specification to filter records where the given field is less than or equal to the
   * specified value.
   *
   * @param fieldName The name of the field to compare.
   * @param value The value to compare against.
   * @param <T> The type of the entity.
   * @return A Specification to filter records where the field is less than or equal to the value.
   */
  public static <T, V extends Comparable<? super V>> Specification<T> isFieldLessThanOrEqualTo(
      final String fieldName, final V value) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), value);
  }

  /**
   * Creates a Sort object based on the given column and direction.
   *
   * @param sortColumn The column to sort by.
   * @param sortDirection The direction of sorting (ASC or DESC).
   * @return A Sort object.
   * @throws IllegalArgumentException If the sort direction is invalid.
   */
  public static <T> Sort createSort(
      final String sortColumn,
      final RequestEnums.SortDirection sortDirection,
      final Class<T> entityClass) {
    if (!StringUtils.hasText(sortColumn)) {
      throw new IllegalArgumentException("Sort column cannot be null or empty");
    }

    validateSortColumn(sortColumn, entityClass);

    Sort.Direction direction;
    if (sortDirection == null) {
      direction = Sort.Direction.ASC;
    } else {
      try {
        direction = Sort.Direction.fromString(sortDirection.name());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid sort direction. Must be 'ASC' or 'DESC'");
      }
    }

    return Sort.by(direction, sortColumn);
  }

  /**
   * Validates if the given sort column exists in the entity class.
   *
   * @param sortColumn The column to validate.
   * @param entityClass The class of the entity.
   * @throws IllegalArgumentException If the column does not exist in the entity.
   */
  private static <T> void validateSortColumn(final String sortColumn, final Class<T> entityClass) {
    List<String> fieldNames =
        Arrays.stream(entityClass.getDeclaredFields()).map(Field::getName).toList();

    if (!fieldNames.contains(sortColumn)) {
      throw new IllegalArgumentException(
          String.format(
              "Sort column [%s] doesn't exist in entity [%s]",
              sortColumn, entityClass.getSimpleName()));
    }
  }

  /**
   * Creates a Pageable object for pagination and sorting.
   *
   * @param pageNumber The page number (zero-based).
   * @param perPageSize The number of items per page.
   * @param sortColumn The column to sort by.
   * @param sortDirection The direction of sorting (ASC or DESC).
   * @param entityClass The class of the entity.
   * @return A Pageable object.
   * @throws IllegalArgumentException If the page or size is invalid, or if the sort column is
   *     invalid.
   */
  public static <T> Pageable createPageable(
      final int pageNumber,
      final int perPageSize,
      final String sortColumn,
      final RequestEnums.SortDirection sortDirection,
      final Class<T> entityClass) {
    final int validatedPageNumber = Math.max(pageNumber, 0);
    final int validatedPerPageSize = (perPageSize <= 10 || perPageSize > 1000) ? 100 : perPageSize;

    // Create the Sort object
    Sort sort = createSort(sortColumn, sortDirection, entityClass);

    // Return a Pageable object with pagination and sorting
    return PageRequest.of(validatedPageNumber, validatedPerPageSize, sort);
  }
}
