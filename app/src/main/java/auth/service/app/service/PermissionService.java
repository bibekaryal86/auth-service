package auth.service.app.service;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.repository.PermissionRepository;
import auth.service.app.util.JpaDataUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
public class PermissionService {

  private final PermissionRepository permissionRepository;
  private final CircularDependencyService circularDependencyService;

  // CREATE
  public PermissionEntity createPermission(final PermissionRequest permissionRequest) {
    log.debug("Create Permission: [{}]", permissionRequest);
    PermissionEntity permissionEntity = new PermissionEntity();
    BeanUtils.copyProperties(permissionRequest, permissionEntity);
    permissionEntity.setRole(
        circularDependencyService.readRole(permissionRequest.getRoleId(), false));
    return permissionRepository.save(permissionEntity);
  }

  // READ
  public Page<PermissionEntity> readPermissions(final RequestMetadata requestMetadata) {
    log.debug("Read Permissions: [{}]", requestMetadata);
    final Pageable pageable = JpaDataUtils.getQueryPageable(requestMetadata);
    final Specification<PermissionEntity> specification =
        JpaDataUtils.getQuerySpecification(requestMetadata);
    return permissionRepository.findAll(specification, pageable);
  }

  public List<PermissionEntity> readPermissionsByRoleIds(final List<Long> roleIds) {
    log.debug("Read Permissions By Role Ids: [{}]", roleIds);
    return permissionRepository.findByRoleIds(roleIds);
  }

  /** Use {@link CircularDependencyService#readPermission(Long, boolean)} */
  private PermissionEntity readPermission(final Long id) {
    log.debug("Read Permission: [{}]", id);
    return permissionRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Permission", String.valueOf(id)));
  }

  // UPDATE
  public PermissionEntity updatePermission(
      final Long id, final PermissionRequest permissionRequest) {
    log.debug("Update Permission: [{}], [{}]", id, permissionRequest);
    final PermissionEntity permissionEntity = readPermission(id);

    if (permissionEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Permission", String.valueOf(id));
    }

    BeanUtils.copyProperties(permissionRequest, permissionEntity);

    if (!Objects.equals(permissionEntity.getRole().getId(), permissionRequest.getRoleId())) {
      permissionEntity.setRole(
          circularDependencyService.readRole(permissionRequest.getRoleId(), false));
    }

    return permissionRepository.save(permissionEntity);
  }

  // DELETE
  public PermissionEntity softDeletePermission(final Long id) {
    log.info("Soft Delete Permission: [{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);

    if (permissionEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Permission", String.valueOf(id));
    }

    permissionEntity.setDeletedDate(LocalDateTime.now());
    return permissionRepository.save(permissionEntity);
  }

  public void hardDeletePermission(final Long id) {
    log.info("Hard Delete Permission: [{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);
    permissionRepository.delete(permissionEntity);
  }

  public void hardDeletePermissionsByRoleId(final Long roleId) {
    log.info("Hard Delete Permission by Role Id: [{}]", roleId);
    permissionRepository.deleteByRoleId(roleId);
  }

  // RESTORE
  public PermissionEntity restoreSoftDeletedPermission(final Long id) {
    log.info("Restore Soft Deleted Permission: [{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);
    permissionEntity.setDeletedDate(null);
    return permissionRepository.save(permissionEntity);
  }
}
