package user.management.system.app.model.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto extends UserRequest {
  private Integer id;
  private UserStatusDto status;
  private boolean isValidated;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private LocalDateTime deletedDate;

  public UserDto() {
    super();
  }

  public UserDto(
      final Integer id,
      final String firstName,
      final String lastName,
      final String email,
      final String phone,
      final UserStatusDto status,
      final boolean isValidated,
      final LocalDateTime createdDate,
      final LocalDateTime updatedDate,
      final LocalDateTime deletedDate) {
    super(firstName, lastName, email, phone, "", 0);
    this.id = id;
    this.status = status;
    this.isValidated = isValidated;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.deletedDate = deletedDate;
  }
}
