package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class PlatformProfileRoleResponse {
  private List<PlatformProfileRoleDto> platformProfileRoles;
  private ResponseMetadata responseMetadata;
}
