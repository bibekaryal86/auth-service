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
import user.management.system.app.model.dto.User;

public class RoleMapper implements ResultSetExtractor<List<Role>> {
  @Override
  public List<Role> extractData(ResultSet rs) throws SQLException, DataAccessException {
    Map<Integer, Role> roles = new HashMap<>();

    while (rs.next()) {
      int roleId = rs.getInt("role_id");
      Role role = roles.get(roleId);

      if (role == null) {
        role =
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
        roles.put(roleId, role);
      }

      int userId = rs.getInt("user_id");
      if (userId > 0) {
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
        role.getUsers().add(user);
      }
    }
    return new ArrayList<>(roles.values());
  }
}
