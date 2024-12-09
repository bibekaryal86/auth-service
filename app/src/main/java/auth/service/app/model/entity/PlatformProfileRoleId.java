package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PlatformProfileRoleId implements Serializable {
  @Column(name = "platform_id", nullable = false)
  private Long platformId;

  @Column(name = "profile_id", nullable = false)
  private Long profileId;

  @Column(name = "role_id", nullable = false)
  private Long roleId;
}
