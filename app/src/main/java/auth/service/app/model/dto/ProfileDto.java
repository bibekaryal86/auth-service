package auth.service.app.model.dto;

import static auth.service.app.util.ConstantUtils.ROLE_NAME_SUPERUSER;

import auth.service.app.model.entity.PlatformEntity;
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
  private Boolean isValidated;
  private Integer loginAttempts;
  private LocalDateTime lastLogin;

  private ProfileAddressDto profileAddress;
  private List<ProfileDtoPlatformRole> platformRoles;
  private List<ProfileDtoRolePlatform> rolePlatforms;

  public AuthToken toAuthToken(final PlatformEntity platformEntity) {
    final AuthTokenPlatform authTokenPlatform =
        AuthTokenPlatform.builder()
            .id(platformEntity.getId())
            .platformName(platformEntity.getPlatformName())
            .build();
    final AuthTokenProfile authTokenProfile =
        AuthTokenProfile.builder().id(this.getId()).email(this.getEmail()).build();
    final List<RoleDto> roleDtos =
        platformRoles.stream()
            .flatMap(profileDtoPlatformRole -> profileDtoPlatformRole.getRoles().stream())
            .toList();
    final List<AuthTokenRole> authTokenRoles =
        CollectionUtils.isEmpty(roleDtos)
            ? Collections.emptyList()
            : roleDtos.stream()
                .map(
                    roleDto ->
                        AuthTokenRole.builder()
                            .id(roleDto.getId())
                            .roleName(roleDto.getRoleName())
                            .build())
                .toList();
    final List<AuthTokenPermission> authTokenPermissions =
        roleDtos.stream()
            .flatMap(
                roleDto ->
                    roleDto.getPermissions() == null
                        ? Stream.empty()
                        : roleDto.getPermissions().stream()
                            .map(
                                permissionDto ->
                                    AuthTokenPermission.builder()
                                        .id(permissionDto.getId())
                                        .permissionName(permissionDto.getPermissionName())
                                        .build()))
            .toList();
    final boolean isSuperUser =
        authTokenRoles.stream()
            .anyMatch(authTokenRole -> authTokenRole.getRoleName().equals(ROLE_NAME_SUPERUSER));
    return AuthToken.builder()
        .platform(authTokenPlatform)
        .profile(authTokenProfile)
        .roles(authTokenRoles)
        .permissions(authTokenPermissions)
        .isSuperUser(isSuperUser)
        .build();
  }
}
