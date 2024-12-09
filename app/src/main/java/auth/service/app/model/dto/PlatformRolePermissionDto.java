package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformRolePermissionDto {
  private PlatformDto platform;
  private RoleDto role;
  private PermissionDto permission;
  private LocalDateTime assignedDate;
}
