package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platform_profile_role")
@Getter
@Setter
@NoArgsConstructor
public class PlatformProfileRoleEntity {

  @EmbeddedId private PlatformProfileRoleId id;

  @MapsId("platformId")
  @ManyToOne
  @JoinColumn(name = "platform_id", nullable = false)
  private PlatformEntity platform;

  @MapsId("profileId")
  @ManyToOne
  @JoinColumn(name = "profile_id", nullable = false)
  private ProfileEntity profile;

  @MapsId("roleId")
  @ManyToOne
  @JoinColumn(name = "role_id", nullable = false)
  private RoleEntity role;

  @Column(name = "assigned_date", nullable = false)
  private LocalDateTime assignedDate;

  @Column(name = "unassigned_date")
  private LocalDateTime unassignedDate;
}
