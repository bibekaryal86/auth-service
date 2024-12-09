package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class PermissionResponse {
  private List<PermissionDto> permissions;
  private ResponseMetadata responseMetadata;
}
