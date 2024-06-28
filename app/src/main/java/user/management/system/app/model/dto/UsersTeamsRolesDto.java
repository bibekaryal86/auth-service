package user.management.system.app.model.dto;

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
public class UsersTeamsRolesDto {
  @Id
  @ManyToOne
  @JoinColumn(name = "user_id")
  private UsersDto user;

  @Id
  @ManyToOne
  @JoinColumn(name = "team_id")
  private TeamsDto team;

  @Id
  @ManyToOne
  @JoinColumn(name = "role_id")
  private RolesDto role;
}
