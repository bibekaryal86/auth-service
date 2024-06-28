package user.management.system.app.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
@Table(name = "roles")
public class RolesDto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name", nullable = false, length = 250, unique = true)
  private String name;

  @Column(name = "desc", nullable = false, columnDefinition = "text")
  private String desc;

  @Column(name = "status", nullable = false, length = 50)
  private String status;

  @Column(name = "created", nullable = false)
  private LocalDateTime created;

  @Column(name = "updated", nullable = false)
  private LocalDateTime updated;

  @Column(name = "deleted")
  private LocalDateTime deleted;

  // relationships
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "users_roles",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private Set<UsersDto> users = new HashSet<>();

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
  private List<UsersProjectsRolesDto> usersProjectsRoles = new ArrayList<>();

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
  private List<UsersTeamsRolesDto> usersTeamsRoles = new ArrayList<>();
}
