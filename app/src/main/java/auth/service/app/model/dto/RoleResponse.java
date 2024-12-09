package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class RoleResponse {
  private List<RoleDto> roles;
  private ResponseMetadata responseMetadata;
}
