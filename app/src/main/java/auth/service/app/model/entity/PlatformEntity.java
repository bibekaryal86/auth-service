package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platform")
@Getter
@Setter
@NoArgsConstructor
public class PlatformEntity extends BaseEntity {
  @Column(name = "platform_name", unique = true, nullable = false)
  private String platformName;

  @Column(name = "platform_desc", nullable = false)
  private String platformDesc;
}
