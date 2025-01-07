package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlatformProfileRoleResponse {
  private List<PlatformProfileRoleDto> platformProfileRoles;
  private ResponseMetadata responseMetadata;
}
