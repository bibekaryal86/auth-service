package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class ProfileResponse {
  private List<ProfileDto> profiles;
  private ResponseMetadata responseMetadata;
}
