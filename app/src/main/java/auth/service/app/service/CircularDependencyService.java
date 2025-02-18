package auth.service.app.service;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.token.AuthToken;
import auth.service.app.repository.AddressTypeRepository;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircularDependencyService {

  private final AddressTypeRepository addressTypeRepository;
  private final PermissionRepository permissionRepository;
  private final PlatformRepository platformRepository;
  private final RoleRepository roleRepository;
  private final ProfileRepository profileRepository;

  private AuthToken getAuthToken() {
    return (AuthToken) SecurityContextHolder.getContext().getAuthentication().getCredentials();
  }

  public AddressTypeEntity readAddressType(final Long id) {
    log.debug("Read Address Type: [{}]", id);

    final AddressTypeEntity addressTypeEntity =
        addressTypeRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Address Type", String.valueOf(id)));

    if (addressTypeEntity.getDeletedDate() == null || getAuthToken().isSuperUser()) {
      return addressTypeEntity;
    } else {
      throw new ElementNotActiveException("entity", "Address Type");
    }
  }

  public PermissionEntity readPermission(final Long id) {
    log.debug("Read Permission: [{}]", id);
    final PermissionEntity permissionEntity =
        permissionRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Permission", String.valueOf(id)));

    if (permissionEntity.getDeletedDate() == null || getAuthToken().isSuperUser()) {
      return permissionEntity;
    } else {
      throw new ElementNotActiveException("entity", "Permission");
    }
  }

  public PlatformEntity readPlatform(final Long id) {
    log.debug("Read Platform: [{}]", id);
    final PlatformEntity platformEntity =
        platformRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Platform", String.valueOf(id)));

    if (platformEntity.getDeletedDate() == null || getAuthToken().isSuperUser()) {
      return platformEntity;
    } else {
      throw new ElementNotActiveException("entity", "Platform");
    }
  }

  public RoleEntity readRole(final Long id) {
    log.debug("Read Role: [{}]", id);
    final RoleEntity roleEntity =
        roleRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Role", String.valueOf(id)));

    if (roleEntity.getDeletedDate() == null || getAuthToken().isSuperUser()) {
      return roleEntity;
    } else {
      throw new ElementNotActiveException("entity", "Role");
    }
  }

  public RoleEntity readRoleByName(final String roleName) {
    log.debug("Read Role By Name: [{}]", roleName);
    final RoleEntity roleEntity =
        roleRepository
            .findOne(getRoleNameExample(roleName))
            .orElseThrow(() -> new ElementNotFoundException("Role", roleName));

    if (roleEntity.getDeletedDate() == null || getAuthToken().isSuperUser()) {
      return roleEntity;
    } else {
      throw new ElementNotActiveException("entity", "Role");
    }
  }

  private Example<RoleEntity> getRoleNameExample(final String roleName) {
    RoleEntity probe = new RoleEntity();
    probe.setRoleName(roleName);

    ExampleMatcher matcher =
        ExampleMatcher.matching()
            .withIgnorePaths("id", "createdDate", "updatedDate", "deletedDate", "roleDesc")
            .withIgnoreNullValues()
            .withMatcher("roleName", ExampleMatcher.GenericPropertyMatchers.exact());
    return Example.of(probe, matcher); // Exact match for roleName
  }

  public ProfileEntity readProfile(final Long id) {
    log.debug("Read Profile: [{}]", id);

    final ProfileEntity profileEntity =
        profileRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Profile", String.valueOf(id)));

    if (profileEntity.getDeletedDate() == null || getAuthToken().isSuperUser()) {
      return profileEntity;
    } else {
      throw new ElementNotActiveException("entity", "Profile");
    }
  }
}
