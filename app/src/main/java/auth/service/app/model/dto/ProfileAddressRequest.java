package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileAddressRequest {
  private Long id;  // needed in request for updates from within profile
  private Long profileId;
  private Long typeId;
  private String street;
  private String city;
  private String state;
  private String country;
  private String postalCode;
}
