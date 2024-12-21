package auth.service.app.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class PlatformProfileRoleRequest {
  @Positive(message = "PlatformID is required")
  private long platformId;

  @Positive(message = "ProfileID is required")
  private long profileId;

  @Positive(message = "RoleId is required")
  private long roleId;
}
