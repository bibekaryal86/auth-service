package user.management.system.app.model.dto;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AddressTypeResponse extends ResponseMetadata {
  private List<AddressTypeDto> addressTypes;

  public AddressTypeResponse(
      final List<AddressTypeDto> addressTypes,
      final ResponseCrudInfo responseCrudInfo,
      final ResponsePageInfo responsePageInfo,
      final ResponseStatusInfo responseStatusInfo) {
    super(responseCrudInfo, responsePageInfo, responseStatusInfo);
    this.addressTypes = addressTypes;
  }
}
