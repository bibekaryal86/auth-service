package auth.service.app.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformRolePermissionDto {
  private PlatformDto platform;
  private RoleDto role;
  private PermissionDto permission;
  private LocalDateTime assignedDate;
}
