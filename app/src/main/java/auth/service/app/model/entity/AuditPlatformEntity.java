package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_platform")
@Getter
@Setter
@NoArgsConstructor
public class AuditPlatformEntity extends BaseEntityAudit {

  @Column(name = "event_data", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private PlatformEntity eventData;

  @ManyToOne
  @JoinColumn(name = "platform_id")
  private PlatformEntity platform;
}
