package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class RolePermissionId implements Serializable {
  @Column(name = "role_id", nullable = false)
  private Integer roleId;

  @Column(name = "permission_id", nullable = false)
  private Integer permissionId;
}
