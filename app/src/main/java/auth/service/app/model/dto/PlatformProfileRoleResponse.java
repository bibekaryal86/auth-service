package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlatformProfileRoleResponse {
  private ResponseMetadata responseMetadata;
}
