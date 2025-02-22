package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
public class RoleEntity extends BaseEntity {
  @Column(name = "role_name", unique = true, nullable = false)
  private String roleName;

  @Column(name = "role_desc", nullable = false)
  private String roleDesc;
}
