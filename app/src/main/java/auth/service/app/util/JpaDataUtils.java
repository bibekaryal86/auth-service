package auth.service.app.util;

import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.token.AuthToken;
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
   * @param requestMetadata Request Metadata.
   * @return A Pageable object.
   */
  public static Pageable getQueryPageable(final RequestMetadata requestMetadata) {
    if (requestMetadata == null) {
      return PageRequest.of(0, 100);
    }
    return PageRequest.of(
        requestMetadata.getPageNumber() - 1,
        requestMetadata.getPerPage(),
        Sort.by(requestMetadata.getSortDirection(), requestMetadata.getSortColumn()));
  }

  /**
   * Creates a Pageable object for pagination and sorting.
   *
   * @param requestMetadata Request Metadata.
   * @return A Pageable object.
   */
  public static Pageable getQueryPageableAudit(final RequestMetadata requestMetadata) {
    if (requestMetadata == null) {
      return PageRequest.of(0, 100);
    }
    return PageRequest.of(
        requestMetadata.getHistoryPage() - 1,
        requestMetadata.getHistorySize(),
        Sort.by(Sort.Direction.DESC, "createdAt"));
  }

  /**
   * Creates a Specification object for querying with filters.
   *
   * @param requestMetadata Request Metadata.
   * @return A Specification object.
   */
  public static <T> Specification<T> getQuerySpecification(final RequestMetadata requestMetadata) {
    Specification<T> specification = Specification.where(null);

    if (!shouldIncludeDeletedRecords(requestMetadata)) {
      specification = specification.and(isFieldNull("deletedDate"));
    }

    return specification;
  }

  public static boolean shouldIncludeDeletedRecords(final RequestMetadata requestMetadata) {
    if (requestMetadata != null && requestMetadata.isIncludeDeleted()) {
      AuthToken authToken = CommonUtils.getAuthentication();
      return authToken.getIsSuperUser();
    }
    return false;
  }
}
