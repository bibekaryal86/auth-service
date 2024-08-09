package user.management.system.app.model.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAddressResponse extends ResponseMetadata {
  private List<UserAddressDto> userAddresses;

  public UserAddressResponse(
      final List<UserAddressDto> userAddresses,
      final ResponseCrudInfo responseCrudInfo,
      final ResponsePageInfo responsePageInfo,
      final ResponseStatusInfo responseStatusInfo) {
    super(responseCrudInfo, responsePageInfo, responseStatusInfo);
    this.userAddresses = userAddresses;
  }
}
