package user.management.system.app.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDto extends PermissionRequest {
  private Integer id;

  public PermissionDto() {
    super();
  }

  public PermissionDto(final Integer id, final String name, final String description) {
    super(name, description);
    this.id = id;
  }
}
