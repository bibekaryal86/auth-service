package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileResponse {
  private List<ProfileDto> profiles;
  private ResponseMetadata responseMetadata;
  private RequestMetadata requestMetadata;
  private AuditResponse auditResponse;
}
