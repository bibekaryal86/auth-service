package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class PlatformProfileRoleResponse {
  private List<PlatformProfileRoleDto> platformProfileRoles;
  private ResponseMetadata responseMetadata;
}
