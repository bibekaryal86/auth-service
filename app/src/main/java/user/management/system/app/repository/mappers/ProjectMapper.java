package user.management.system.app.repository.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.CollectionUtils;
import user.management.system.app.model.dto.Project;
import user.management.system.app.model.dto.Role;
import user.management.system.app.model.dto.User;
import user.management.system.app.model.dto.UserRole;

public class ProjectMapper implements ResultSetExtractor<List<Project>> {
  @Override
  public List<Project> extractData(ResultSet rs) throws SQLException, DataAccessException {
    Map<Integer, Project> projects = new HashMap<>();

    while (rs.next()) {
      int projectId = rs.getInt("project_id");
      Project project = projects.get(projectId);

      if (project == null) {
        project =
            Project.builder()
                .id(projectId)
                .name(rs.getString("project_name"))
                .description(rs.getString("project_desc"))
                .status(rs.getString("project_status"))
                .created(rs.getTimestamp("project_created").toLocalDateTime())
                .updated(rs.getTimestamp("project_updated").toLocalDateTime())
                .deleted(
                    rs.getTimestamp("project_deleted") == null
                        ? null
                        : rs.getTimestamp("project_deleted").toLocalDateTime())
                .startDate(
                    rs.getTimestamp("start_date") == null
                        ? null
                        : rs.getTimestamp("start_date").toLocalDateTime())
                .endDate(
                    rs.getTimestamp("end_date") == null
                        ? null
                        : rs.getTimestamp("end_date").toLocalDateTime())
                .build();
        projects.put(projectId, project);
      }

      if (CollectionUtils.isEmpty(project.getUserRoles())) {
        project.setUserRoles(new ArrayList<>());
      }

      int userId = rs.getInt("user_id");
      int roleId = rs.getInt("role_id");
      if (userId > 0 && roleId > 0) {
        User user =
            User.builder()
                .id(userId)
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("user_email"))
                .status(rs.getString("user_status"))
                .created(rs.getTimestamp("user_created").toLocalDateTime())
                .updated(rs.getTimestamp("user_updated").toLocalDateTime())
                .deleted(
                    rs.getTimestamp("user_deleted") == null
                        ? null
                        : rs.getTimestamp("user_deleted").toLocalDateTime())
                .build();
        Role role =
            Role.builder()
                .id(roleId)
                .name(rs.getString("role_name"))
                .description(rs.getString("role_desc"))
                .status(rs.getString("role_status"))
                .created(rs.getTimestamp("role_created").toLocalDateTime())
                .updated(rs.getTimestamp("role_updated").toLocalDateTime())
                .deleted(
                    rs.getTimestamp("role_deleted") == null
                        ? null
                        : rs.getTimestamp("role_deleted").toLocalDateTime())
                .build();
        UserRole userRole = UserRole.builder().user(user).role(role).build();
        project.getUserRoles().add(userRole);
      }
    }
    return new ArrayList<>(projects.values());
  }
}
