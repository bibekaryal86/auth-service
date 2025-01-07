package auth.service.app.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class PlatformProfileRoleRequest {
  @NotNull(message = "PlatformID is required")
  @Min(value = 1, message = "PlatformID is required")
  private Long platformId;

  @NotNull(message = "ProfileID is required")
  @Min(value = 1, message = "ProfileID is required")
  private Long profileId;

  @NotNull(message = "RoleID is required")
  @Min(value = 1, message = "RoleID is required")
  private Long roleId;
}
