package auth.service.app.model.dto;

import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.model.token.AuthTokenPermission;
import auth.service.app.model.token.AuthTokenPlatform;
import auth.service.app.model.token.AuthTokenProfile;
import auth.service.app.model.token.AuthTokenRole;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
  private StatusTypeDto status;

  private Map<PlatformDto, List<RoleDto>> platformRolesMap;

  public AuthToken toAuthToken(final PlatformEntity platformEntity) {
    AuthTokenPlatform authTokenPlatform =
        AuthTokenPlatform.builder()
            .id(platformEntity.getId())
            .platformName(platformEntity.getPlatformName())
            .build();
    AuthTokenProfile authTokenProfile =
        AuthTokenProfile.builder()
            .id(this.getId())
            .email(this.getEmail())
            .statusId(this.getStatusId())
            .isValidated(this.isValidated())
            .isDeleted(this.getDeletedDate() != null)
            .build();
    List<RoleDto> roleDtos =
        platformRolesMap.entrySet().stream()
            .filter(entry -> entry.getKey().getId().equals(platformEntity.getId()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(Collections.emptyList());
    List<AuthTokenRole> authTokenRoles =
        CollectionUtils.isEmpty(roleDtos)
            ? Collections.emptyList()
            : roleDtos.stream()
                .map(
                    appRoleDto ->
                        AuthTokenRole.builder()
                            .id(appRoleDto.getId())
                            .roleName(appRoleDto.getRoleName())
                            .build())
                .toList();
    List<AuthTokenPermission> authTokenPermissions =
        roleDtos.stream()
            .flatMap(
                roleDto ->
                    roleDto.getPlatformPermissionsMap().entrySet().stream()
                        .filter(entry -> entry.getKey().getId().equals(platformEntity.getId()))
                        .flatMap(entry -> entry.getValue().stream())
                        .map(
                            permissionDto ->
                                AuthTokenPermission.builder()
                                    .id(permissionDto.getId())
                                    .permissionName(permissionDto.getPermissionName())
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
