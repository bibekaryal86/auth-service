package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 50)
  private String lastName;

  @Column(name = "email", nullable = false, length = 250, unique = true)
  private String email;

  @Column(name = "password", nullable = false, length = 250)
  private transient String password;

  @Column(name = "status", nullable = false, length = 50)
  private String status;

  @Column(name = "created", nullable = false)
  private LocalDateTime created;

  @Column(name = "updated", nullable = false)
  private LocalDateTime updated;

  @Column(name = "deleted")
  private LocalDateTime deleted;

  @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
  private Set<UsersAuditEntity> userAudits = new HashSet<>();

  @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
  private Set<ProjectsAuditEntity> projectAudits = new HashSet<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private Set<UsersAuditEntity> userAudits2 = new HashSet<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
  private List<UserProjectRoleEntity> usersProjectsRoles = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
  private List<UserTeamRoleEntity> usersTeamsRoles = new ArrayList<>();

  @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
  private Set<RoleEntity> roles = new HashSet<>();
}
