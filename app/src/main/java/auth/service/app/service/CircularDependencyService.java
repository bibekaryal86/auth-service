package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.model.entity.StatusTypeEntity;
import auth.service.app.repository.AddressTypeRepository;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.repository.ProfileRepository;
import auth.service.app.repository.RoleRepository;
import auth.service.app.repository.StatusTypeRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircularDependencyService {

  private final AddressTypeService addressTypeService;
  private final AddressTypeRepository addressTypeRepository;
  private final PermissionService permissionService;
  private final PermissionRepository permissionRepository;
  private final PlatformService platformService;
  private final PlatformRepository platformRepository;
  private final RoleService roleService;
  private final RoleRepository roleRepository;
  private final StatusTypeService statusTypeService;
  private final StatusTypeRepository statusTypeRepository;
  private final ProfileRepository profileRepository;

  public AddressTypeEntity readAddressType(final Long id) {
    log.debug("Read Address Type: [{}]", id);
    return addressTypeService.readAddressTypes().stream()
        .filter(addressType -> Objects.equals(addressType.getId(), id))
        .findFirst()
        .orElseGet(
            () ->
                addressTypeRepository
                    .findById(id)
                    .orElseThrow(
                        () -> new ElementNotFoundException("Address Type", String.valueOf(id))));
  }

  public PermissionEntity readPermission(final Long id) {
    log.debug("Read Permission: [{}]", id);
    return permissionService.readPermissions().stream()
        .filter(addressType -> Objects.equals(addressType.getId(), id))
        .findFirst()
        .orElseGet(
            () ->
                permissionRepository
                    .findById(id)
                    .orElseThrow(
                        () -> new ElementNotFoundException("Permission", String.valueOf(id))));
  }

  public PlatformEntity readPlatform(final Long id) {
    log.debug("Read Platform: [{}]", id);
    return platformService.readPlatforms().stream()
        .filter(addressType -> Objects.equals(addressType.getId(), id))
        .findFirst()
        .orElseGet(
            () ->
                platformRepository
                    .findById(id)
                    .orElseThrow(
                        () -> new ElementNotFoundException("Platform", String.valueOf(id))));
  }

  public RoleEntity readRole(final Long id) {
    log.debug("Read Role: [{}]", id);
    return roleService.readRoles().stream()
        .filter(addressType -> Objects.equals(addressType.getId(), id))
        .findFirst()
        .orElseGet(
            () ->
                roleRepository
                    .findById(id)
                    .orElseThrow(() -> new ElementNotFoundException("Role", String.valueOf(id))));
  }

  public RoleEntity readRoleByName(final String roleName) {
    log.debug("Read Role By Name: [{}]", roleName);
    return roleService.readRoles().stream()
        .filter(role -> Objects.equals(role.getRoleName(), roleName))
        .findFirst()
        .orElseGet(
            () ->
                roleRepository
                    .findOne(getRoleNameExample(roleName))
                    .orElseThrow(() -> new ElementNotFoundException("Role", roleName)));
  }

  public StatusTypeEntity readStatusType(final Long id) {
    log.debug("Read Status Type: [{}]", id);
    return statusTypeService.readStatusTypes().stream()
        .filter(addressType -> Objects.equals(addressType.getId(), id))
        .findFirst()
        .orElseGet(
            () ->
                statusTypeRepository
                    .findById(id)
                    .orElseThrow(
                        () -> new ElementNotFoundException("Status Type", String.valueOf(id))));
  }

  public List<StatusTypeEntity> readStatusTypesByComponentName(final String componentName) {
    log.debug("Read Status Types by Component Name: [{}]", componentName);
    List<StatusTypeEntity> statusTypeEntities =
        statusTypeService.readStatusTypes().stream()
            .filter(statusType -> Objects.equals(statusType.getComponentName(), componentName))
            .toList();
    if (CollectionUtils.isEmpty(statusTypeEntities)) {
      return statusTypeRepository.findByComponentNameOrderByStatusNameAsc(componentName);
    }
    return statusTypeEntities;
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
    return profileRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Profile", String.valueOf(id)));
  }
}
