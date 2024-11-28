package auth.service.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AppsDto extends AppsRequest {
  private String id;

  public AppsDto() {
    super();
  }

  public AppsDto(final String id, final String name, final String description) {
    super(name, description);
    this.id = id;
  }
}
