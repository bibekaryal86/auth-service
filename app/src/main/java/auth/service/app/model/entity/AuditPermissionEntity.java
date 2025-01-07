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
@Table(name = "audit_permission")
@Getter
@Setter
@NoArgsConstructor
public class AuditPermissionEntity extends BaseEntityAudit {

  @Column(name = "event_data", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private PermissionEntity eventData;

  @ManyToOne
  @JoinColumn(name = "permission_id")
  private PermissionEntity permission;
}
