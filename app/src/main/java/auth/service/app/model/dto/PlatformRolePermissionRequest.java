package auth.service.app.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformRolePermissionRequest {
  @Positive(message = "PlatformID is required")
  private int platformId;

  @Positive(message = "RoleId is required")
  private int roleId;

  @Positive(message = "PermissionID is required")
  private int permissionId;
}
