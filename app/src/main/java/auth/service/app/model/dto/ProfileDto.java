package auth.service.app.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
  private Long id;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private LocalDateTime deletedDate;

  private String firstName;
  private String lastName;
  @ToString.Exclude private String email;
  @ToString.Exclude private String phone;
  private Long statusId;
  private boolean isValidated;
  private Integer loginAttempts;

  private List<ProfileAddressDto> addresses;
  private ProfileStatusDto status;
  private List<RoleDto> roles;

  // TODO public AuthToken toAuthToken(final Long platformId) {}
}
