package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppRoleRequest;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.repository.AppRoleRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppRoleService {

  private final AppRoleRepository appRoleRepository;

  // CREATE
  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public AppRoleEntity createAppRole(final AppRoleRequest appRoleRequest) {
    log.debug("Create App Role: [{}]", appRoleRequest);
    AppRoleEntity appRoleEntity = new AppRoleEntity();
    BeanUtils.copyProperties(appRoleRequest, appRoleEntity);
    return appRoleRepository.save(appRoleEntity);
  }

  // READ
  @Cacheable(value = "roles")
  public List<AppRoleEntity> readAppRoles() {
    log.debug("Read App Roles...");
    return appRoleRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
  }

  public AppRoleEntity readAppRole(final int id) {
    log.debug("Read App Role: [{}]", id);
    return appRoleRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Role", String.valueOf(id)));
  }

  // UPDATE
  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public AppRoleEntity updateAppRole(final int id, final AppRoleRequest appRoleRequest) {
    log.debug("Update App Role: [{}], [{}]", id, appRoleRequest);
    final AppRoleEntity appRoleEntity = readAppRole(id);
    BeanUtils.copyProperties(appRoleRequest, appRoleEntity);
    return appRoleRepository.save(appRoleEntity);
  }

  // DELETE
  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public AppRoleEntity softDeleteAppRole(final int id) {
    log.info("Soft Delete App Role: [{}]", id);
    final AppRoleEntity appRoleEntity = readAppRole(id);
    appRoleEntity.setDeletedDate(LocalDateTime.now());
    return appRoleRepository.save(appRoleEntity);
  }

  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public void hardDeleteAppRole(final int id) {
    log.info("Hard Delete App Role: [{}]", id);
    final AppRoleEntity appRoleEntity = readAppRole(id);
    appRoleRepository.delete(appRoleEntity);
  }

  // RESTORE
  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public AppRoleEntity restoreSoftDeletedAppRole(final int id) {
    log.info("Restore Soft Deleted App Role: [{}]", id);
    final AppRoleEntity appRoleEntity = readAppRole(id);
    appRoleEntity.setDeletedDate(null);
    return appRoleRepository.save(appRoleEntity);
  }
}
