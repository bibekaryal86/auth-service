package auth.service.app.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class PlatformRolePermissionRequest {
  @NotNull(message = "PlatformID is required")
  @Min(value = 1, message = "PlatformID is required")
  private Long platformId;

  @NotNull(message = "RoleID is required")
  @Min(value = 1, message = "RoleID is required")
  private Long roleId;

  @NotNull(message = "PermissionID is required")
  @Min(value = 1, message = "PermissionID is required")
  private Long permissionId;
}
