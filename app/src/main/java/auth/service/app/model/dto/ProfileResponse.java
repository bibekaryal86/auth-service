package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class ProfileResponse {
  private List<ProfileDto> profiles;
  private ResponseMetadata responseMetadata;
}
