package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class ProfileStatusResponse {
  private List<ProfileStatusDto> profileStatuses;
  private ResponseMetadata responseMetadata;
}
