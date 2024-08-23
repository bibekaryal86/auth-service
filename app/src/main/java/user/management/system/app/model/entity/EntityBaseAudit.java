package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class EntityBaseAudit {
  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "event_desc")
  private String eventDesc;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "created_by", insertable = false, updatable = false)
  private AppUserEntity createdBy;

  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  @Column(name = "user_agent", nullable = false)
  private String userAgent;
}
