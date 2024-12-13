package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PermissionRequest;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.repository.PermissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

  private final PermissionRepository permissionRepository;

  // CREATE
  @CacheEvict(value = "permissions", allEntries = true, beforeInvocation = true)
  public PermissionEntity createPermission(final PermissionRequest permissionRequest) {
    log.debug("Create Permission: [{}]", permissionRequest);
    PermissionEntity permissionEntity = new PermissionEntity();
    BeanUtils.copyProperties(permissionRequest, permissionEntity);
    return permissionRepository.save(permissionEntity);
  }

  // READ
  public List<PermissionEntity> readPermissions() {
    log.debug("Read Permissions...");
    return permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "permissionName"));
  }

  /** Use {@link ReadFromCacheService#readPermission(Long)} */
  public PermissionEntity readPermission(final Long id) {
    log.debug("Read Permission: [{}]", id);
    return permissionRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Permission", String.valueOf(id)));
  }

  // UPDATE
  @CacheEvict(value = "permissions", allEntries = true, beforeInvocation = true)
  public PermissionEntity updatePermission(
      final Long id, final PermissionRequest permissionRequest) {
    log.debug("Update Permission: [{}], [{}]", id, permissionRequest);
    final PermissionEntity permissionEntity = readPermission(id);
    BeanUtils.copyProperties(permissionRequest, permissionEntity);
    return permissionRepository.save(permissionEntity);
  }

  // DELETE
  @CacheEvict(value = "permissions", allEntries = true, beforeInvocation = true)
  public PermissionEntity softDeletePermission(final Long id) {
    log.info("Soft Delete Permission: [{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);
    permissionEntity.setDeletedDate(LocalDateTime.now());
    return permissionRepository.save(permissionEntity);
  }

  @CacheEvict(value = "permissions", allEntries = true, beforeInvocation = true)
  @Transactional
  public void hardDeletePermission(final Long id) {
    log.info("Hard Delete Permission: [{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);
    permissionRepository.delete(permissionEntity);
  }

  // RESTORE
  @CacheEvict(value = "permissions", allEntries = true, beforeInvocation = true)
  public PermissionEntity restoreSoftDeletedPermission(final Long id) {
    log.info("Restore Soft Deleted Permission: [{}]", id);
    final PermissionEntity permissionEntity = readPermission(id);
    permissionEntity.setDeletedDate(null);
    return permissionRepository.save(permissionEntity);
  }
}
