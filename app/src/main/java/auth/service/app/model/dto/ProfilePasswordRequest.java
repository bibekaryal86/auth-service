package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePasswordRequest {
  @NotBlank(message = "Email is Required")
  private String email;

  @NotBlank(message = "Password is Required")
  @ToString.Exclude
  private String password;
}
