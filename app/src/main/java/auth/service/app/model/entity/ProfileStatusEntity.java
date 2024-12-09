package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile_status")
@Getter
@Setter
@NoArgsConstructor
public class ProfileStatusEntity extends BaseEntity {
  @Column(name = "status_name", unique = true, nullable = false)
  private String statusName;

  @Column(name = "status_desc", nullable = false)
  private String statusDesc;
}
