package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class PlatformRolePermissionResponse {
  private List<PlatformRolePermissionDto> platformRolePermissions;
  private ResponseMetadata responseMetadata;
}
