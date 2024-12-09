package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class PlatformResponse {
  private List<PlatformDto> platforms;
  private ResponseMetadata responseMetadata;
}
