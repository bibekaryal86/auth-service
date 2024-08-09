package user.management.system.app.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserAddressDto extends UserAddressRequest {
  private Integer id;
  private UserDto user;
  private AddressTypeDto addressType;

  public UserAddressDto() {
    super();
  }

  public UserAddressDto(
      final Integer id,
      final String street,
      final String city,
      final String state,
      final String country,
      final String postalCode,
      final UserDto user,
      final AddressTypeDto addressType) {
    super(0, 0, street, city, state, country, postalCode);
    this.id = id;
    this.user = user;
    this.addressType = addressType;
  }
}
