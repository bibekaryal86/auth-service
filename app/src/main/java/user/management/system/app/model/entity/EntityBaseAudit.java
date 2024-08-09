package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class EntityBaseAudit {
  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "event_data", columnDefinition = "jsonb", nullable = false)
  private String eventData;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "created_by", insertable = false, updatable = false)
  private UserEntity createdBy;

  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  @Column(name = "user_agent", nullable = false)
  private String userAgent;
}
