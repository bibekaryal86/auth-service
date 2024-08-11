package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AppUserAddressDto extends AppUserAddressRequest {
  private Integer id;
  private AppUserDto user;

  public AppUserAddressDto() {
    super();
  }

  public AppUserAddressDto(
      final Integer id,
      final String addressType,
      final String street,
      final String city,
      final String state,
      final String country,
      final String postalCode,
      final AppUserDto user) {
    super(0, addressType, street, city, state, country, postalCode);
    this.id = id;
    this.user = user;
  }
}
