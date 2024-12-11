package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class StatusTypeResponse {
  private List<StatusTypeDto> profileStatuses;
  private ResponseMetadata responseMetadata;
}
