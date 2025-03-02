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
public class RoleDto {
  private Long id;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private LocalDateTime deletedDate;

  private String roleName;
  private String roleDesc;

  private List<PermissionDto> permissions;
  private List<RoleDtoPlatformProfile> platformProfiles;
  private List<RoleDtoProfilePlatform> profilePlatforms;
}
