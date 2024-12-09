package auth.service.app.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class AddressTypeResponse {
  private List<AddressTypeDto> addressTypes;
  private ResponseMetadata responseMetadata;
}
