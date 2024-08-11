package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AppAppRoleDto extends AppRoleRequest {
  private Integer id;

  public AppAppRoleDto() {
    super();
  }

  public AppAppRoleDto(final Integer id, final String name, final String description) {
    super(name, description);
    this.id = id;
  }
}
