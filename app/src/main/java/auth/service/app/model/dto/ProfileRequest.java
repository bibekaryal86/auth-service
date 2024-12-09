package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class ProfileRequest {
  @NotBlank(message = "First Name is required")
  private String firstName;

  @NotBlank(message = "Last Name is required")
  private String lastName;

  @NotBlank(message = "Email is required")
  private String email;

  private String phone;

  @ToString.Exclude
  private String password;

  @NotBlank(message = "Status is required")
  private Long statusId;

  private boolean guestUser;
  private List<AppUserAddressDto> addresses;
}
