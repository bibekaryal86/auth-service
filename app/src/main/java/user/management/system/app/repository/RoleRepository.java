package user.management.system.app.repository;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import user.management.system.app.model.dto.Role;
import user.management.system.app.model.dto.RoleRequest;
import user.management.system.app.repository.mappers.RoleMapper;

@Repository
public class RoleRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert simpleJdbcInsert;

  public RoleRepository(NamedParameterJdbcTemplate jdbcTemplate, DataSource dataSource) {
    this.jdbcTemplate = jdbcTemplate;
    this.simpleJdbcInsert =
        new SimpleJdbcInsert(dataSource)
            .withTableName("roles")
            .usingGeneratedKeyColumns("id")
            .usingColumns("name", "description", "status");
  }

  private static final String SQL_GET_ALL_ROLES =
      "WITH LimitedRoles AS ( "
          + "SELECT * "
          + "FROM roles "
          + "WHERE (:includeDeletedRoles = TRUE OR deleted IS NULL) "
          + "ORDER BY name "
          + "LIMIT :limit OFFSET :offset"
          + ") "
          + "SELECT "
          + "r.id AS role_id, "
          + "r.name AS role_name, "
          + "r.description AS role_desc, "
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
  private static final String SQL_GET_ONE_ROLE =
      "WITH LimitedRoles AS ( "
          + "SELECT * "
          + "FROM roles "
          + "WHERE id = :id AND (:includeDeletedRoles = TRUE OR deleted IS NULL) "
          + ") "
          + "SELECT "
          + "r.id AS role_id, "
          + "r.name AS role_name, "
          + "r.description AS role_desc, "
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
          + "(:includeDeletedUsers = TRUE OR u.deleted IS NULL) ";
  private static final String SQL_DELETE_ROLE_HARD = "DELETE FROM roles WHERE id = :id";
  private static final String SQL_DELETE_ROLE_SOFT =
      "UPDATE roles SET deleted = NOW(), updated = NOW() WHERE id = :id";
  private static final String SQL_RESTORE_ROLE_SOFT =
      "UPDATE roles SET deleted = NULL, updated = NOW() WHERE id = :id";

  public List<Role> getAllRoles(
      final int limit,
      final int offset,
      final boolean includeDeletedRoles,
      final boolean includeDeletedUsers) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("limit", limit)
            .addValue("offset", offset)
            .addValue("includeDeletedRoles", includeDeletedRoles)
            .addValue("includeDeletedUsers", includeDeletedUsers);
    return this.jdbcTemplate.query(SQL_GET_ALL_ROLES, parameters, new RoleMapper());
  }

  public Role getRoleById(
      final int id, final boolean includeDeletedRoles, final boolean includeDeletedUsers) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("includeDeletedRoles", includeDeletedRoles)
            .addValue("includeDeletedUsers", includeDeletedUsers);
    List<Role> roles = this.jdbcTemplate.query(SQL_GET_ONE_ROLE, parameters, new RoleMapper());
    if (CollectionUtils.isEmpty(roles)) {
      return null;
    } else {
      return roles.getFirst();
    }
  }

  public int createRole(final RoleRequest role) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("name", role.getName())
            .addValue("description", role.getDescription())
            .addValue("status", role.getStatus());
    return this.simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
  }

  public int updateRole(final int id, final RoleRequest role) {
    StringBuilder sql = new StringBuilder("UPDATE roles SET updated = NOW() ");
    if (StringUtils.hasText(role.getName())) {
      sql.append(", name = :name ");
    }
    if (StringUtils.hasText(role.getDescription())) {
      sql.append(", description = :description ");
    }
    if (StringUtils.hasText(role.getStatus())) {
      sql.append(", status = :status ");
    }
    sql.append("WHERE id = :id ");
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("name", role.getName())
            .addValue("description", role.getDescription())
            .addValue("status", role.getStatus());
    return this.jdbcTemplate.update(sql.toString(), parameters);
  }

  public int deleteRole(final int id, final boolean isHardDelete) {
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
    if (isHardDelete) {
      return this.jdbcTemplate.update(SQL_DELETE_ROLE_HARD, parameters);
    } else {
      return this.jdbcTemplate.update(SQL_DELETE_ROLE_SOFT, parameters);
    }
  }

  public int restoreRole(final int id) {
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
    return this.jdbcTemplate.update(SQL_RESTORE_ROLE_SOFT, parameters);
  }
}
