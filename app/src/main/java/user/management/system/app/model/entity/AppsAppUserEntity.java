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
@Table(name = "app_user_app")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class AppsAppUserEntity {

  @EmbeddedId private AppsAppUserId id;

  @MapsId("appId")
  @ManyToOne
  @JoinColumn(name = "app_id", nullable = false)
  private AppsEntity app;

  @MapsId("appUserId")
  @ManyToOne
  @JoinColumn(name = "app_user_id", nullable = false)
  private AppUserEntity appUser;

  @Column(name = "assigned_date", nullable = false)
  private LocalDateTime assignedDate;
}
