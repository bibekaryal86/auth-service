package user.management.system.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  @ToString.Exclude private transient String password;
  private int statusId;
}
