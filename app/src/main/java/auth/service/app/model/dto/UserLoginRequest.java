package auth.service.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {
  @NotBlank(message = "REQUIRED")
  private String email;

  @NotBlank(message = "REQUIRED")
  @ToString.Exclude
  private String password;
}
