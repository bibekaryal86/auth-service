package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMetadata {
  private ResponseCrudInfo responseCrudInfo;
  private ResponsePageInfo responsePageInfo;
  private ResponseStatusInfo responseStatusInfo;
}
