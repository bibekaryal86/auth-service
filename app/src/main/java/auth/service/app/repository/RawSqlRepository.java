package auth.service.app.repository;

import auth.service.app.model.token.AuthTokenRolePermissionLookup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RawSqlRepository {

  private final EntityManager entityManager;

  @SuppressWarnings({"rawtypes"})
  private List queryList(final String sql, final Map<String, Object> params) {
    final Query query = entityManager.createNativeQuery(sql);
    params.forEach(query::setParameter);
    return query.getResultList();
  }

  public List<AuthTokenRolePermissionLookup> findRolePermissionsForAuthToken(
      final Long platformId, final Long profileId) {
    final String sql =
        """
        SELECT DISTINCT
            r.id AS role_id,
            r.role_name AS role_name,
            p.id AS permission_id,
            p.permission_name AS permission_name
        FROM platform_profile_role ppr
        JOIN role r ON r.id = ppr.role_id
        JOIN platform_role_permission prp
            ON  prp.platform_id = ppr.platform_id
            AND prp.role_id = ppr.role_id
        JOIN permission p ON p.id = prp.permission_id
        WHERE ppr.platform_id = :platformId
          AND ppr.profile_id  = :profileId
          AND ppr.unassigned_date IS NULL
          AND prp.unassigned_date IS NULL
    """;

    Map<String, Object> params =
        Map.of(
            "platformId", platformId,
            "profileId", profileId);

    @SuppressWarnings({"unchecked"})
    List<Object[]> rows = queryList(sql, params);

    List<AuthTokenRolePermissionLookup> result = new ArrayList<>();

    for (Object[] row : rows) {
      result.add(
          new AuthTokenRolePermissionLookup(
              ((Number) row[0]).longValue(),
              (String) row[1],
              ((Number) row[2]).longValue(),
              (String) row[3]));
    }

    return result;
  }
}
