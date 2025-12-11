package auth.service.app.service;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.repository.PermissionRepository;
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
public class PermissionService {

  private final PermissionRepository permissionRepository;
  private final PlatformRolePermissionService platformRolePermissionService;

  // CREATE
  public PermissionEntity createPermission(final PermissionRequest permissionRequest) {
    log.debug("Create Permission: PermissionRequest=[{}]", permissionRequest);
    PermissionEntity permissionEntity = new PermissionEntity();
    BeanUtils.copyProperties(permissionRequest, permissionEntity);
    return permissionRepository.save(permissionEntity);
  }

  // READ
  public List<PermissionEntity> readPermissions(final boolean isIncludeDeleted) {
    log.debug("Read Permissions: IsIncludeDeleted=[{}]", isIncludeDeleted);
    return permissionRepository.findAllPermissions(isIncludeDeleted);
  }

  /** Use {@link CircularDependencyService#readPermission(Long, boolean)} */
  private PermissionEntity readPermission(final Long id) {
    log.debug("Read Permission: Id=[{}]", id);
    return permissionRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Permission", String.valueOf(id)));
  }

  // UPDATE
  public PermissionEntity updatePermission(
      final Long id, final PermissionRequest permissionRequest) {
    log.debug("Update Permission: Id=[{}], PermissionRequest=[{}]", id, permissionRequest);
    final PermissionEntity permissionEntity = readPermission(id);

    if (permissionEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Permission", String.valueOf(id));
    }

    BeanUtils.copyProperties(permissionRequest, permissionEntity);

    return permissionRepository.save(permissionEntity);
  }

  // DELETE
  public PermissionEntity softDeletePermission(final Long id) {
    log.info("Soft Delete Permission: Id=[{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);

    if (permissionEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Permission", String.valueOf(id));
    }

    permissionEntity.setDeletedDate(LocalDateTime.now());
    return permissionRepository.save(permissionEntity);
  }

  @Transactional
  public void hardDeletePermission(final Long id) {
    log.info("Hard Delete Permission: Id=[{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);

    // before Role can be deleted, we need to delete entities in PlatformRolePermission
    platformRolePermissionService.hardDeletePlatformRolePermissionsByPermissionIds(List.of(id));

    permissionRepository.delete(permissionEntity);
  }

  // RESTORE
  public PermissionEntity restoreSoftDeletedPermission(final Long id) {
    log.info("Restore Soft Deleted Permission: Id=[{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);
    permissionEntity.setDeletedDate(null);
    return permissionRepository.save(permissionEntity);
  }
}
