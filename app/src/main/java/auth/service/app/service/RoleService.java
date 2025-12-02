package auth.service.app.service;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.RoleRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final PlatformProfileRoleService platformProfileRoleService;
  private final PlatformRolePermissionService platformRolePermissionService;

  // CREATE
  public RoleEntity createRole(final RoleRequest roleRequest) {
    log.debug("Create Role: [{}]", roleRequest);
    RoleEntity roleEntity = new RoleEntity();
    BeanUtils.copyProperties(roleRequest, roleEntity);
    return roleRepository.save(roleEntity);
  }

  // READ
  public List<RoleEntity> readRoles(final boolean isIncludeDeleted) {
    log.debug("Read Roles: [{}]", isIncludeDeleted);
    return roleRepository.findAllRoles(isIncludeDeleted);
  }

  /** Use {@link CircularDependencyService#readRole(Long, boolean)} */
  private RoleEntity readRole(final Long id) {
    log.debug("Read Role: [{}]", id);
    return roleRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Role", String.valueOf(id)));
  }

  // UPDATE
  public RoleEntity updateRole(final Long id, final RoleRequest roleRequest) {
    log.debug("Update Role: [{}], [{}]", id, roleRequest);
    final RoleEntity roleEntity = readRole(id);

    if (roleEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Role", String.valueOf(id));
    }

    BeanUtils.copyProperties(roleRequest, roleEntity);
    return roleRepository.save(roleEntity);
  }

  // DELETE
  public RoleEntity softDeleteRole(final Long id) {
    log.info("Soft Delete Role: [{}]", id);
    final RoleEntity roleEntity = readRole(id);

    if (roleEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Role", String.valueOf(id));
    }

    roleEntity.setDeletedDate(LocalDateTime.now());
    return roleRepository.save(roleEntity);
  }

  @Transactional
  public void hardDeleteRole(final Long id) {
    log.info("Hard Delete Role: [{}]", id);
    final RoleEntity roleEntity = readRole(id);

    // before Role can be deleted, we need to delete entities in PlatformProfileRole
    platformProfileRoleService.hardDeletePlatformProfileRolesByRoleIds(List.of(id));
    // before Role can be deleted, we need to delete entities in PlatformRolePermission
    platformRolePermissionService.hardDeletePlatformRolePermissionsByRoleIds(List.of(id));

    roleRepository.delete(roleEntity);
  }

  // RESTORE
  public RoleEntity restoreSoftDeletedRole(final Long id) {
    log.info("Restore Soft Deleted Role: [{}]", id);
    final RoleEntity roleEntity = readRole(id);
    roleEntity.setDeletedDate(null);
    return roleRepository.save(roleEntity);
  }
}
