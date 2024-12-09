package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class RoleResponse {
  private List<RoleDto> roles;
  private ResponseMetadata responseMetadata;
}
