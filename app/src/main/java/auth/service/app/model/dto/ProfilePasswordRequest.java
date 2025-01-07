package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ProfilePasswordRequest {
  @NotBlank(message = "Email is Required")
  private String email;

  @NotBlank(message = "Password is Required")
  @ToString.Exclude
  private String password;
}
