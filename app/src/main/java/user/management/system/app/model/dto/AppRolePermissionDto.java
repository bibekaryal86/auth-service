package user.management.system.app.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppRolePermissionDto {
  private AppRoleDto role;
  private AppPermissionDto permission;
  private LocalDateTime assignedDate;
}
