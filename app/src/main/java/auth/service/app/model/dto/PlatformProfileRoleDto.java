package auth.service.app.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformProfileRoleDto {
  private PlatformDto platform;
  private ProfileDto profile;
  private RoleDto role;

  private LocalDateTime assignedDate;
  private LocalDateTime unassignedDate;
}
