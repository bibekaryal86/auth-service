package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class ProfileStatusResponse {
  private List<ProfileStatusDto> profileStatuses;
  private ResponseMetadata responseMetadata;
}
