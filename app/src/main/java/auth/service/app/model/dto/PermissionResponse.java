package auth.service.app.model.dto;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PermissionResponse {
  private List<PermissionDto> permissions;
  private ResponseMetadata responseMetadata;
  private RequestMetadata requestMetadata;
}
