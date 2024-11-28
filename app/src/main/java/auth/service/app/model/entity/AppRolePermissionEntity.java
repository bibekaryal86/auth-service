package auth.service.app.model.entity;

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
@Table(name = "app_role_permission")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class AppRolePermissionEntity {

  @EmbeddedId private AppRolePermissionId id;

  @MapsId("appRoleId")
  @ManyToOne
  @JoinColumn(name = "app_role_id", nullable = false)
  private AppRoleEntity appRole;

  @MapsId("appPermissionId")
  @ManyToOne
  @JoinColumn(name = "app_permission_id", nullable = false)
  private AppPermissionEntity appPermission;

  @Column(name = "assigned_date", nullable = false)
  private LocalDateTime assignedDate;
}
