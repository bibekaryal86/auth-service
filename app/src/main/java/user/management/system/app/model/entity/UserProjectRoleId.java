package user.management.system.app.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
public class UserProjectRoleId {
  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "project_id", nullable = false)
  private Integer projectId;

  @Column(name = "role_id", nullable = false)
  private Integer roleId;
}
