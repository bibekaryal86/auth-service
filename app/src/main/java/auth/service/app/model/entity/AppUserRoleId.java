package auth.service.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRoleId implements Serializable {
  @Column(name = "app_user_id", nullable = false)
  private Integer appUserId;

  @Column(name = "app_role_id", nullable = false)
  private Integer appRoleId;
}
