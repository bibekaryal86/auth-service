package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlatformRolePermissionResponse {
  private List<PlatformRolePermissionDto> platformRolePermissions;
  private ResponseMetadata responseMetadata;
}
