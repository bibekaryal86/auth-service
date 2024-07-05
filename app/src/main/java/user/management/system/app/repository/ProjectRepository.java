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
import user.management.system.app.model.dto.Project;
import user.management.system.app.model.dto.ProjectRequest;
import user.management.system.app.repository.mappers.ProjectMapper;

@Repository
public class ProjectRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert simpleJdbcInsert;

  public ProjectRepository(NamedParameterJdbcTemplate jdbcTemplate, DataSource dataSource) {
    this.jdbcTemplate = jdbcTemplate;
    this.simpleJdbcInsert =
        new SimpleJdbcInsert(dataSource)
            .withTableName("projects")
            .usingGeneratedKeyColumns("id")
            .usingColumns("name", "description", "status", "start_date", "end_date");
  }

  private static final String SQL_GET_ALL_PROJECTS =
      "WITH LimitedProjects AS ( "
          + "SELECT * "
          + "FROM projects "
          + "WHERE (:includeDeletedProjects = TRUE OR deleted IS NULL) "
          + "ORDER BY name "
          + "LIMIT :limit OFFSET :offset"
          + ") "
          + "SELECT "
          + "p.id AS project_id, "
          + "p.name AS project_name, "
          + "p.description AS project_desc, "
          + "p.status AS project_status, "
          + "p.created AS project_created, "
          + "p.updated AS project_updated, "
          + "p.deleted AS project_deleted, "
          + "p.start_date AS start_date, "
          + "p.end_date AS end_date, "
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
          + "LimitedProjects p "
          + "LEFT JOIN "
          + "users_roles_projects urp ON p.id = urp.project_id "
          + "LEFT JOIN "
          + "users u ON urp.user_id = u.id AND (:includeDeletedUsers = TRUE OR u.deleted IS NULL) "
          + "LEFT JOIN "
          + "roles r ON urp.role_id = r.id AND (:includeDeletedRoles = TRUE OR r.deleted IS NULL) "
          + "ORDER BY "
          + "p.name";
  private static final String SQL_GET_ONE_PROJECT =
      "WITH LimitedProjects AS ( "
          + "SELECT * "
          + "FROM projects "
          + "WHERE id = :id AND (:includeDeletedProjects = TRUE OR deleted IS NULL) "
          + ") "
          + "SELECT "
          + "p.id AS project_id, "
          + "p.name AS project_name, "
          + "p.description AS project_desc, "
          + "p.status AS project_status, "
          + "p.created AS project_created, "
          + "p.updated AS project_updated, "
          + "p.deleted AS project_deleted, "
          + "p.start_date AS start_date, "
          + "p.end_date AS end_date, "
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
          + "LimitedProjects p "
          + "LEFT JOIN "
          + "users_roles_projects urp ON p.id = urp.project_id "
          + "LEFT JOIN "
          + "users u ON urp.user_id = u.id AND (:includeDeletedUsers = TRUE OR u.deleted IS NULL) "
          + "LEFT JOIN "
          + "roles r ON urp.role_id = r.id AND (:includeDeletedRoles = TRUE OR r.deleted IS NULL) "
          + "ORDER BY "
          + "p.name";
  private static final String SQL_DELETE_PROJECT_HARD = "DELETE FROM projects WHERE id = :id";
  private static final String SQL_DELETE_PROJECT_SOFT =
      "UPDATE projects SET deleted = NOW(), updated = NOW() WHERE id = :id";
  private static final String SQL_RESTORE_PROJECT_SOFT =
      "UPDATE projects SET deleted = NULL, updated = NOW() WHERE id = :id";

  public List<Project> getAllProjects(
      final int limit,
      final int offset,
      final boolean includeDeletedProjects,
      final boolean includeDeletedUsers,
      final boolean includeDeletedRoles) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("limit", limit)
            .addValue("offset", offset)
            .addValue("includeDeletedProjects", includeDeletedProjects)
            .addValue("includeDeletedUsers", includeDeletedUsers)
            .addValue("includeDeletedRoles", includeDeletedRoles);
    return this.jdbcTemplate.query(SQL_GET_ALL_PROJECTS, parameters, new ProjectMapper());
  }

  public Project getProjectById(
      final int id,
      final boolean includeDeletedProjects,
      final boolean includeDeletedUsers,
      final boolean includeDeletedRoles) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("includeDeletedProjects", includeDeletedProjects)
            .addValue("includeDeletedUsers", includeDeletedUsers)
            .addValue("includeDeletedRoles", includeDeletedRoles);
    List<Project> projects =
        this.jdbcTemplate.query(SQL_GET_ONE_PROJECT, parameters, new ProjectMapper());
    if (CollectionUtils.isEmpty(projects)) {
      return null;
    } else {
      return projects.getFirst();
    }
  }

  public int createProject(final ProjectRequest project) {
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("name", project.getName())
            .addValue("description", project.getDescription())
            .addValue("status", project.getStatus())
            .addValue("start_date", project.getStartDate())
            .addValue("end_date", project.getEndDate());
    return this.simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
  }

  public int updateProject(final int id, final ProjectRequest project) {
    StringBuilder sql = new StringBuilder("UPDATE projects SET updated = NOW() ");
    if (StringUtils.hasText(project.getName())) {
      sql.append(", name = :name ");
    }
    if (StringUtils.hasText(project.getDescription())) {
      sql.append(", description = :description ");
    }
    if (StringUtils.hasText(project.getStatus())) {
      sql.append(", status = :status ");
    }
    if (project.getStartDate() != null) {
      sql.append(", start_date = :startDate ");
    }
    if (project.getEndDate() != null) {
      sql.append(", end_date = :endDate ");
    }
    sql.append("WHERE id = :id ");
    SqlParameterSource parameters =
        new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("name", project.getName())
            .addValue("description", project.getDescription())
            .addValue("status", project.getStatus())
            .addValue("start_date", project.getStartDate())
            .addValue("end_date", project.getEndDate());
    return this.jdbcTemplate.update(sql.toString(), parameters);
  }

  public int deleteProject(final int id, final boolean isHardDelete) {
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
    if (isHardDelete) {
      return this.jdbcTemplate.update(SQL_DELETE_PROJECT_HARD, parameters);
    } else {
      return this.jdbcTemplate.update(SQL_DELETE_PROJECT_SOFT, parameters);
    }
  }

  public int restoreProject(final int id) {
    SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
    return this.jdbcTemplate.update(SQL_RESTORE_PROJECT_SOFT, parameters);
  }
}
