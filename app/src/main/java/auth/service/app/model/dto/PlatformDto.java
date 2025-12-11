package auth.service.app.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformDto {
  private Long id;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private LocalDateTime deletedDate;

  private String platformName;
  private String platformDesc;

  private List<PlatformRolePermissionDto> platformRolePermissions;
  private List<PlatformProfileRoleDto> platformProfileRoles;
  private List<AuditPlatformDto> history;
}
