package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformProfileRoleDto {
  private PlatformDto platform;
  private ProfileDto profile;
  private RoleDto role;
  private LocalDateTime assignedDate;
}
