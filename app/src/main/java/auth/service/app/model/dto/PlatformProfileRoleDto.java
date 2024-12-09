package auth.service.app.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformProfileRoleDto {
  private PlatformDto platform;
  private ProfileDto profile;
  private RoleDto role;
  private LocalDateTime assignedDate;
}
