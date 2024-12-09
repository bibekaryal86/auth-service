package auth.service.app.model.dto;

import auth.service.app.model.token.AuthToken;
import auth.service.app.model.token.AuthTokenPermission;
import auth.service.app.model.token.AuthTokenPlatform;
import auth.service.app.model.token.AuthTokenProfile;
import auth.service.app.model.token.AuthTokenRole;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

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

  public AuthToken toAuthToken(final PlatformDto platform) {
    AuthTokenPlatform authTokenPlatform =
        AuthTokenPlatform.builder()
            .id(platform.getId())
            .platformName(platform.getPlatformName())
            .build();
    AuthTokenProfile authTokenProfile =
        AuthTokenProfile.builder()
            .id(this.getId())
            .email(this.getEmail())
            .statusId(this.getStatusId())
            .isValidated(this.isValidated())
            .isDeleted(this.getDeletedDate() != null)
            .build();
    List<AuthTokenRole> authTokenRoles =
        CollectionUtils.isEmpty(this.getRoles())
            ? Collections.emptyList()
            : this.getRoles().stream()
                .map(
                    appRoleDto ->
                        AuthTokenRole.builder()
                            .id(appRoleDto.getId())
                            .roleName(appRoleDto.getRoleName())
                            .build())
                .toList();
    List<AuthTokenPermission> authTokenPermissions =
        CollectionUtils.isEmpty(this.getRoles())
            ? Collections.emptyList()
            : this.getRoles().stream()
                .flatMap(
                    appRoleDto ->
                        CollectionUtils.isEmpty(appRoleDto.getPermissions())
                            ? Stream.empty()
                            : appRoleDto.getPermissions().stream()
                                .map(
                                    appPermissionDto ->
                                        AuthTokenPermission.builder()
                                            .id(appPermissionDto.getId())
                                            .permissionName(appPermissionDto.getPermissionName())
                                            .build()))
                .toList();
    return AuthToken.builder()
        .platform(authTokenPlatform)
        .profile(authTokenProfile)
        .roles(authTokenRoles)
        .permissions(authTokenPermissions)
        .build();
  }
}
