package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
public class RolePermissionEntity {

  @EmbeddedId private RolePermissionId id;

  @MapsId("roleId")
  @ManyToOne
  @JoinColumn(name = "role_id", nullable = false)
  private RoleEntity role;

  @MapsId("permissionId")
  @ManyToOne
  @JoinColumn(name = "permission_id", nullable = false)
  private PermissionEntity permission;

  @Column(name = "assigned_date", nullable = false)
  private LocalDateTime assignedDate;
}
