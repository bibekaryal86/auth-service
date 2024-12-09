package auth.service.app.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformProfileRoleRequest {
  @Positive(message = "PlatformID is required")
  private int platformId;

  @Positive(message = "ProfileID is required")
  private int profileId;

  @Positive(message = "RoleId is required")
  private int roleId;
}
