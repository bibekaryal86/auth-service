package user.management.system.app.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectStatusDto extends ProjectStatusRequest {
  private Integer id;

  public ProjectStatusDto() {
    super();
  }

  public ProjectStatusDto(final Integer id, final String name, final String description) {
    super(name, description);
    this.id = id;
  }
}
