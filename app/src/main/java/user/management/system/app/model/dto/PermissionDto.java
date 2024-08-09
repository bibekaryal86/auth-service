package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
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
