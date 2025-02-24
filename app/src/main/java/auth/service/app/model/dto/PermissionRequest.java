package auth.service.app.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class PermissionRequest {
  @NotNull(message = "RoleID is required")
  @Min(value = 1, message = "RoleID is required")
  private Long roleId;

  @NotBlank(message = "Name is required")
  private String permissionName;

  @NotBlank(message = "Description is required")
  private String permissionDesc;
}
