package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
public class PermissionEntity extends BaseEntity {
  @Column(name = "permission_name", nullable = false)
  private String permissionName;

  @Column(name = "permission_desc", nullable = false)
  private String permissionDesc;
}
