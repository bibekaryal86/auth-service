package user.management.system.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {
  @NotBlank private String email;
  @NotBlank @ToString.Exclude private String password;
}
