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
@Table(name = "app_user_role")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class AppUserRoleEntity {

  @EmbeddedId private AppUserRoleId id;

  @MapsId("appUserId")
  @ManyToOne
  @JoinColumn(name = "app_user_id", nullable = false)
  private AppUserEntity appUser;

  @MapsId("appRoleId")
  @ManyToOne
  @JoinColumn(name = "app_role_id", nullable = false)
  private AppRoleEntity appRole;

  @Column(name = "assigned_date")
  private LocalDateTime assignedDate;
}
