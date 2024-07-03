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
import user.management.system.app.model.dto.Team;
import user.management.system.app.repository.mappers.TeamMapper;

@Repository
public class TeamRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert simpleJdbcInsert;

  public TeamRepository(NamedParameterJdbcTemplate jdbcTemplate, DataSource dataSource) {
    this.jdbcTemplate = jdbcTemplate;
    this.simpleJdbcInsert =
        new SimpleJdbcInsert(dataSource)
            .withTableName("teams")
            .usingGeneratedKeyColumns("id")
            .usingColumns("name", "description", "status");
  }

  private static final String SQL_GET_ALL_TEAMS =
      "WITH LimitedTeams AS ( "
          + "SELECT * "
          + "FROM teams "
          + "WHERE (:includeDeletedTeams = TRUE OR deleted IS NULL) "
          + "ORDER BY name "
          + "LIMIT :limit OFFSET :offset"
          + ") "
          + "SELECT "
          + "t.id AS team_id, "
          + "t.name AS team_name, "
          + "t.description AS team_desc, "
          + "t.status AS team_status, "
          + "t.created AS team_created, "
          + "t.updated AS team_updated, "
          + "t.deleted AS team_deleted, "
          + "u.id AS user_id, "
          + "u.first_name AS first_name, "
          + "u.last_name AS last_name, "
          + "u.email AS user_email, "
          + "u.status AS user_status, "
          + "u.created AS user_created, "
          + "u.updated AS user_updated, "
          + "u.deleted AS user_deleted, "
          + "r.id AS role_id, "
          + "r.name AS role_name, "
          + "r.description AS role_desc, "
          + "r.status AS role_status, "
          + "r.created AS role_created, "
          + "r.updated AS role_updated, "
          + "r.deleted AS role_deleted "
          + "FROM "
          + "LimitedTeams t "
          + "LEFT JOIN "
          + "users_roles_teams urt ON t.id = urt.team_id "
          + "LEFT JOIN "
          + "users u ON urt.user_id = u.id AND (:includeDeletedUsers = TRUE OR u.deleted IS NULL) "
          + "LEFT JOIN "
          + "roles r ON urt.role_id = r.id AND (:includeDeletedRoles = TRUE OR r.deleted IS NULL) "
          + "ORDER BY "
          + "t.name";
  private static final String SQL_GET_ONE_TEAM =
      "WITH LimitedTeams AS ( "
          + "SELECT * "
          + "FROM teams "
          + "WHERE id = :id AND (:includeDeletedTeams = TRUE OR deleted IS NULL) "
          + ") "
          + "SELECT "
          + "t.id AS team_id, "
          + "t.name AS team_name, "
          + "t.description AS team_desc, "
          + "t.status AS team_status, "
          + "t.created AS team_created, "
          + "t.updated AS team_updated, "
          + "t.deleted AS team_deleted, "
          + "u.id AS user_id, "
          + "u.first_name AS first_name, "
          + "u.last_name AS last_name, "
          + "u.email AS user_email, "
          + "u.status AS user_status, "
          + "u.created AS user_created, "
          + "u.updated AS user_updated, "
          + "u.deleted AS user_deleted, "
          + "r.id AS role_id, "
          + "r.name AS role_name, "
          + "r.description AS role_desc, "
          + "r.status AS role_status, "
          + "r.created AS role_created, "
          + "r.updated AS role_updated, "
          + "r.deleted AS role_deleted "
          + "FROM "
          + "LimitedTeams t "
          + "LEFT JOIN "
          + "users_roles_teams urt ON t.id = urt.team_id "
          + "LEFT JOIN "
          + "users u ON urt.user_id = u.id AND (:includeDeletedUsers = TRUE OR u.deleted IS NULL) "
          + "LEFT JOIN "
          + "roles r ON urt.role_id = r.id AND (:includeDeletedRoles = TRUE OR u.deleted IS NULL) "
          + "ORDER BY "
          + "t.name";
  private static final String SQL_DELETE_TEAM_HARD = "DELETE FROM teams WHERE id = :id";
  private static final String SQL_DELETE_TEAM_SOFT =
      "UPDATE teams SET deleted = NOW(), updated = NOW() WHERE id = :id";
  private static final String SQL_RESTORE_TEAM_SOFT =
      "UPDATE teams SET deleted = NULL, updated = NOW() WHERE id = :id";

  public List<Team> getAllTeams(
      final int limit,
      final int offset,
      final boolean includeDeletedTeams,
      final boolean includeDeletedUsers,
      final boolean includeDeletedRoles) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("limit", limit)
            .addValue("offset", offset)
            .addValue("includeDeletedTeams", includeDeletedTeams)
            .addValue("includeDeletedUsers", includeDeletedUsers)
            .addValue("includeDeletedRoles", includeDeletedRoles);
    return this.jdbcTemplate.query(SQL_GET_ALL_TEAMS, parameters, new TeamMapper());
  }

  public Team getTeamById(
      final int id,
      final boolean includeDeletedTeams,
      final boolean includeDeletedUsers,
      final boolean includeDeletedRoles) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("includeDeletedTeams", includeDeletedTeams)
            .addValue("includeDeletedUsers", includeDeletedUsers)
            .addValue("includeDeletedRoles", includeDeletedRoles);
    List<Team> teams = this.jdbcTemplate.query(SQL_GET_ONE_TEAM, parameters, new TeamMapper());
    if (CollectionUtils.isEmpty(teams)) {
      return null;
    } else {
      return teams.getFirst();
    }
  }

  public int createTeam(final String name, final String description, final String status) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("name", name)
            .addValue("description", description)
            .addValue("status", status);
    return this.simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
  }

  public int updateTeam(
      final int id, final String name, final String description, final String status) {
    StringBuilder sql = new StringBuilder("UPDATE teams SET updated = NOW() ");
    if (StringUtils.hasText(name)) {
      sql.append(", name = :name ");
    }
    if (StringUtils.hasText(description)) {
      sql.append(", description = :description ");
    }
    if (StringUtils.hasText(status)) {
      sql.append(", status = :status ");
    }
    sql.append("WHERE id = :id ");
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("name", name)
            .addValue("description", description)
            .addValue("status", status);
    return this.jdbcTemplate.update(sql.toString(), parameters);
  }

  public int deleteTeam(final int id, final boolean isHardDelete) {
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
    if (isHardDelete) {
      return this.jdbcTemplate.update(SQL_DELETE_TEAM_HARD, parameters);
    } else {
      return this.jdbcTemplate.update(SQL_DELETE_TEAM_SOFT, parameters);
    }
  }

  public int restoreTeam(final int id) {
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
    return this.jdbcTemplate.update(SQL_RESTORE_TEAM_SOFT, parameters);
  }
}
