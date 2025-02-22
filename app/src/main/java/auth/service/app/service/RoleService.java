package auth.service.app.service;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.RoleRepository;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.JpaDataUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final PlatformProfileRoleService platformProfileRoleService;
  private final PermissionService permissionService;

  // CREATE
  public RoleEntity createRole(final RoleRequest roleRequest) {
    log.debug("Create Role: [{}]", roleRequest);
    RoleEntity roleEntity = new RoleEntity();
    BeanUtils.copyProperties(roleRequest, roleEntity);
    return roleRepository.save(roleEntity);
  }

  // READ
  public Page<RoleEntity> readRoles(final RequestMetadata requestMetadata) {
    log.debug("Read Roles: [{}]", requestMetadata);

    final RequestMetadata requestMetadataToUse =
        CommonUtils.isRequestMetadataIncluded(requestMetadata)
            ? requestMetadata
            : CommonUtils.defaultRequestMetadata("roleName");
    final Pageable pageable = JpaDataUtils.getQueryPageable(requestMetadataToUse, "roleName");
    final Specification<RoleEntity> specification =
        JpaDataUtils.getQuerySpecification(requestMetadataToUse);
    return roleRepository.findAll(specification, pageable);
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

  public void hardDeleteRole(final Long id) {
    log.info("Hard Delete Role: [{}]", id);
    final RoleEntity roleEntity = readRole(id);

    // before Role can be deleted, we need to delete entities in PlatformProfileRole
    platformProfileRoleService.hardDeletePlatformProfileRolesByRoleIds(List.of(id));
    // before Role can be deleted, we need to delete entities in Permission
    permissionService.hardDeletePermissionsByRoleId(id);
    // now Role can be deleted
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
