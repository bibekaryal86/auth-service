package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users_projects_roles")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectRole {

  @EmbeddedId private UserProjectRoleId id;

  @MapsId("userId")
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @MapsId("projectId")
  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @MapsId("roleId")
  @ManyToOne
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Column(name = "assigned_date")
  private LocalDateTime assignedDate;
}
