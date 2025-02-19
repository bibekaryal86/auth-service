package auth.service.app.service;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import auth.service.app.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircularDependencyService {

  private final PermissionRepository permissionRepository;
  private final PlatformRepository platformRepository;
  private final RoleRepository roleRepository;
  private final ProfileRepository profileRepository;

  public PermissionEntity readPermission(final Long id, final boolean isIncludeDeleted) {
    log.debug("Read Permission: [{}] | [{}]", id, isIncludeDeleted);
    final PermissionEntity permissionEntity =
        permissionRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Permission", String.valueOf(id)));

    if (permissionEntity.getDeletedDate() == null
        || (isIncludeDeleted && CommonUtils.getAuthentication().isSuperUser())) {
      return permissionEntity;
    } else {
      throw new ElementNotActiveException("Permission", String.valueOf(id));
    }
  }

  public PlatformEntity readPlatform(final Long id, final boolean isIncludeDeleted) {
    log.debug("Read Platform: [{}] | [{}]", id, isIncludeDeleted);
    final PlatformEntity platformEntity =
        platformRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Platform", String.valueOf(id)));

    if (platformEntity.getDeletedDate() == null
        || (isIncludeDeleted && CommonUtils.getAuthentication().isSuperUser())) {
      return platformEntity;
    } else {
      throw new ElementNotActiveException("Platform", String.valueOf(id));
    }
  }

  public RoleEntity readRole(final Long id, final boolean isIncludeDeleted) {
    log.debug("Read Role: [{}] | [{}]", id, isIncludeDeleted);
    final RoleEntity roleEntity =
        roleRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Role", String.valueOf(id)));

    if (roleEntity.getDeletedDate() == null
        || (isIncludeDeleted && CommonUtils.getAuthentication().isSuperUser())) {
      return roleEntity;
    } else {
      throw new ElementNotActiveException("Role", String.valueOf(id));
    }
  }

  public RoleEntity readRoleByName(final String roleName, final boolean isIncludeDeleted) {
    log.debug("Read Role By Name: [{}] | [{}]", roleName, isIncludeDeleted);
    final RoleEntity roleEntity =
        roleRepository
            .findOne(getRoleNameExample(roleName))
            .orElseThrow(() -> new ElementNotFoundException("Role", roleName));

    if (roleEntity.getDeletedDate() == null
        || (isIncludeDeleted && CommonUtils.getAuthentication().isSuperUser())) {
      return roleEntity;
    } else {
      throw new ElementNotActiveException("Role", roleName);
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

  public ProfileEntity readProfile(final Long id, final boolean isIncludeDeleted) {
    log.debug("Read Profile: [{}] | [{}]", id, isIncludeDeleted);

    final ProfileEntity profileEntity =
        profileRepository
            .findById(id)
            .orElseThrow(() -> new ElementNotFoundException("Profile", String.valueOf(id)));

    if (profileEntity.getDeletedDate() == null
        || (isIncludeDeleted && CommonUtils.getAuthentication().isSuperUser())) {
      return profileEntity;
    } else {
      throw new ElementNotActiveException("Profile", String.valueOf(id));
    }
  }
}
