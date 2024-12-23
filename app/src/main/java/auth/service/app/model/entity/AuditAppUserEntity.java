package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_app_user")
@Getter
@Setter
@NoArgsConstructor
public class AuditAppUserEntity extends EntityBaseAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "event_data", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private AppUserEntity eventData;

  @ManyToOne
  @JoinColumn(name = "app_user_id")
  private AppUserEntity appUser;
}
