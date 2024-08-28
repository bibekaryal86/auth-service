package user.management.system.app.model.dto;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AppRoleDto extends AppRoleRequest {
  private Integer id;

  private List<AppPermissionDto> permissions;

  public AppRoleDto() {
    super();
  }

  public AppRoleDto(final Integer id, final String name, final String description) {
    super(name, description);
    this.id = id;
  }
}
