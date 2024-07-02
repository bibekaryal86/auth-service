package user.management.system.app.repository;

import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import user.management.system.app.model.dto.Role;
import user.management.system.app.repository.mappers.RoleMapper;

@Repository
public class RoleRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public RoleRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final String SQL_GET_ALL_ROLES =
      "WITH LimitedRoles AS ( "
          + "SELECT * "
          + "FROM roles "
          + "WHERE (:includeDeleted = TRUE OR deleted IS NULL) "
          + "ORDER BY name "
          + "LIMIT :limit OFFSET :offset"
          + ") "
          + "SELECT "
          + "r.id AS role_id, "
          + "r.name AS role_name, "
          + "r.desc AS role_desc, "
          + "r.status AS role_status, "
          + "r.created AS role_created, "
          + "r.updated AS role_updated, "
          + "r.deleted AS role_deleted, "
          + "u.id AS user_id, "
          + "u.first_name AS first_name, "
          + "u.last_name AS last_name, "
          + "u.email AS user_email, "
          + "u.status AS user_status, "
          + "u.created AS user_created, "
          + "u.updated AS user_updated, "
          + "u.deleted AS user_deleted "
          + "FROM "
          + "LimitedRoles r "
          + "LEFT JOIN "
          + "users_roles ur ON r.id = ur.role_id "
          + "LEFT JOIN "
          + "users u ON ur.user_id = u.id "
          + "WHERE "
          + "(:includeDeletedUsers = TRUE OR u.deleted IS NULL) "
          + "ORDER BY "
          + "r.name";

  public List<Role> getAllRoles(
      int offset, int limit, boolean includeDeletedRoles, boolean includeDeletedUsers) {
    MapSqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("limit", limit)
            .addValue("offset", offset)
            .addValue("includeDeletedRoles", includeDeletedRoles)
            .addValue("includeDeletedUsers", includeDeletedUsers);
    return this.jdbcTemplate.query(SQL_GET_ALL_ROLES, parameters, new RoleMapper());
  }
}
