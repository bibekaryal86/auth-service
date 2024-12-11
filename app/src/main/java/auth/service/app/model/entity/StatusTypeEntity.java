package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "status_type",
    uniqueConstraints = @UniqueConstraint(columnNames = {"component_name", "status_name"}))
@Getter
@Setter
@NoArgsConstructor
public class StatusTypeEntity extends BaseEntity {
  @Column(name = "component_name", nullable = false)
  private String componentName;

  @Column(name = "status_name", nullable = false)
  private String statusName;

  @Column(name = "status_desc", nullable = false)
  private String statusDesc;
}
