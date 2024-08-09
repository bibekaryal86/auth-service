package user.management.system.app.model.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse extends ResponseMetadata {
  private List<UserDto> users;

  public UserResponse(
      final List<UserDto> users,
      final ResponseCrudInfo responseCrudInfo,
      final ResponsePageInfo responsePageInfo,
      final ResponseStatusInfo responseStatusInfo) {
    super(responseCrudInfo, responsePageInfo, responseStatusInfo);
    this.users = users;
  }
}
