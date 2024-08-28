package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserAddressDto {
  private Integer id;
  private String addressType;
  private String street;
  private String city;
  private String state;
  private String country;
  private String postalCode;
}
