package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AppPermissionDto extends AppPermissionRequest {
  private Integer id;
  private String appId;

  public AppPermissionDto() {
    super();
  }

  public AppPermissionDto(
      final Integer id, final String appId, final String name, final String description) {
    super(name, description);
    this.id = id;
    this.appId = appId;
  }
}
