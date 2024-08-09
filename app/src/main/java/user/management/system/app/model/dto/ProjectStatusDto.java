package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
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
