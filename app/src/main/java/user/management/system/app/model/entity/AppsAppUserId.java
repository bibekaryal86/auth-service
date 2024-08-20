package user.management.system.app.model.entity;

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
public class AppsAppUserId implements Serializable {
  @Column(name = "app_id", nullable = false)
  private String appId;

  @Column(name = "appUserId", nullable = false)
  private Integer userId;
}
