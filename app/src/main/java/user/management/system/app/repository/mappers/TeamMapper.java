package user.management.system.app.repository.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import user.management.system.app.model.dto.Role;
import user.management.system.app.model.dto.Team;
import user.management.system.app.model.dto.User;
import user.management.system.app.model.dto.UserRole;

public class TeamMapper implements ResultSetExtractor<List<Team>> {
  @Override
  public List<Team> extractData(ResultSet rs) throws SQLException, DataAccessException {
    Map<Integer, Team> teams = new HashMap<>();

    while (rs.next()) {
      int teamId = rs.getInt("team_id");
      Team team = teams.get(teamId);

      if (team == null) {
        team =
            Team.builder()
                .id(teamId)
                .name(rs.getString("team_name"))
                .description(rs.getString("team_desc"))
                .status(rs.getString("team_status"))
                .created(rs.getTimestamp("team_created").toLocalDateTime())
                .updated(rs.getTimestamp("team_updated").toLocalDateTime())
                .deleted(
                    rs.getTimestamp("team_deleted") == null
                        ? null
                        : rs.getTimestamp("team_deleted").toLocalDateTime())
                .build();
        teams.put(teamId, team);
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
        team.getUserRoles().add(userRole);
      }
    }
    return new ArrayList<>(teams.values());
  }
}
