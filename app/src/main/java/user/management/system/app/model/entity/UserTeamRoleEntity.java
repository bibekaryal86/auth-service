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
@Table(name = "users_teams_roles")
public class UserTeamRoleEntity {
  @Id
  @ManyToOne
  @JoinColumn(name = "user_id")
  private UserEntity user;

  @Id
  @ManyToOne
  @JoinColumn(name = "team_id")
  private TeamEntity team;

  @Id
  @ManyToOne
  @JoinColumn(name = "role_id")
  private RoleEntity role;
}
