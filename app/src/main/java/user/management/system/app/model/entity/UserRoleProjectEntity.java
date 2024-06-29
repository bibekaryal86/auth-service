package user.management.system.app.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users_roles_projects")
public class UserRoleProjectEntity {
  @Id
  private Integer id;
  private Integer userId;
  private Integer roleId;
  private Integer projectId;
}
