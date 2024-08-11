package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AppPermissionDto extends AppPermissionRequest {
  private Integer id;

  public AppPermissionDto() {
    super();
  }

  public AppPermissionDto(
      final Integer id, final String app, final String name, final String description) {
    super(app, name, description);
    this.id = id;
  }
}
