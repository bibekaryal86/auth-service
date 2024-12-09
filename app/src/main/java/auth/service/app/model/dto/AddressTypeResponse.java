package auth.service.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class AddressTypeResponse {
  private List<AddressTypeDto> addressTypes;
  private ResponseMetadata responseMetadata;
}
