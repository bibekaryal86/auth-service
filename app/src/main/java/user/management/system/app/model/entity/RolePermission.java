package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles_permissions")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
  @Id
  @ManyToOne
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Id
  @ManyToOne
  @JoinColumn(name = "permission_id", nullable = false)
  private Permission permission;

  @Column(name = "assigned_date")
  private LocalDateTime assignedDate;
}
