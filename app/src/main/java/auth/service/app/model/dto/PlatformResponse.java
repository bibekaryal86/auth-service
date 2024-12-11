package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class PlatformResponse {
  private List<PlatformDto> platforms;
  private ResponseMetadata responseMetadata;
}