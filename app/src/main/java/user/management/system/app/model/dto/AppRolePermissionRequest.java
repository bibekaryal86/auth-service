package user.management.system.app.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppRolePermissionRequest {
  @Positive(message = "RoleID is required")
  private int roleId;

  @Positive(message = "PermissionID is required")
  private int permissionId;
}
