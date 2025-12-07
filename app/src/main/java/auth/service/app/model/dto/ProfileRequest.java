package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
  @NotBlank(message = "First Name is required")
  private String firstName;

  @NotBlank(message = "Last Name is required")
  private String lastName;

  @NotBlank(message = "Email is required")
  private String email;

  private String phone;

  @ToString.Exclude private String password;

  private boolean guestUser;
  private ProfileAddressRequest addressRequest;
}
