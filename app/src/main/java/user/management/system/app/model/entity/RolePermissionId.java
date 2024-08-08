package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionId {
  @Column(name = "role_id", nullable = false)
  private Integer roleId;

  @Column(name = "permission_id", nullable = false)
  private Integer permissionId;
}
