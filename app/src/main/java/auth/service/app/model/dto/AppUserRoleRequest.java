package auth.service.app.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRoleRequest {
  @Positive(message = "UserID is required")
  private int userId;

  @Positive(message = "RoleID is required")
  private int roleId;
}
