package user.management.system.app.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users_projects_roles")
public class UserProjectRoleEntity {
  @Id
  @ManyToOne
  @JoinColumn(name = "user_id")
  private UserEntity user;

  @Id
  @ManyToOne
  @JoinColumn(name = "project_id")
  private ProjectEntity project;

  @Id
  @ManyToOne
  @JoinColumn(name = "role_id")
  private RoleEntity role;
}
