package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserStatusDto extends UserStatusRequest {
  private Integer id;

  public UserStatusDto() {
    super();
  }

  public UserStatusDto(final Integer id, final String name, final String description) {
    super(name, description);
    this.id = id;
  }
}
