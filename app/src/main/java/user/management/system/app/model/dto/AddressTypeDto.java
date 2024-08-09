package user.management.system.app.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressTypeDto extends AddressTypeRequest {
  private Integer id;

  public AddressTypeDto() {
    super();
  }

  public AddressTypeDto(final Integer id, final String name, final String description) {
    super(name, description);
    this.id = id;
  }
}
