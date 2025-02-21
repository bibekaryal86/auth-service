package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ProfileAddressRequest {
  private Long id; // needed in request for updates from within profile
  private Long profileId;
  private String street;
  private String city;
  private String state;
  private String country;
  private String postalCode;
  private boolean isDeleteAddress;
}
