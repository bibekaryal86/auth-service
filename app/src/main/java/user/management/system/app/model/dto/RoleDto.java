package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RoleDto extends RoleRequest {
  private Integer id;

  public RoleDto() {
    super();
  }

  public RoleDto(final Integer id, final String name, final String description) {
    super(name, description);
    this.id = id;
  }
}
