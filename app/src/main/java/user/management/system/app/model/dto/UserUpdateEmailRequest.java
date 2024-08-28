package user.management.system.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateEmailRequest {
  @NotBlank(message = "REQUIRED")
  private String oldEmail;

  @NotBlank(message = "REQUIRED")
  private String newEmail;
}
