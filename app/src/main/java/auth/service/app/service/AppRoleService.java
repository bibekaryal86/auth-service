package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppRoleRequest;
import auth.service.app.model.entity.AppRoleEntity;
import auth.service.app.repository.AppRoleRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppRoleService {

  private final AppRoleRepository appRoleRepository;

  // CREATE
  @CacheEvict(value = "role", allEntries = true, beforeInvocation = true)
  public AppRoleEntity createAppRole(final AppRoleRequest appRoleRequest) {
    log.debug("Create App Role: [{}]", appRoleRequest);
    AppRoleEntity appRoleEntity = new AppRoleEntity();
    BeanUtils.copyProperties(appRoleRequest, appRoleEntity);
    return appRoleRepository.save(appRoleEntity);
  }

  // READ
  @Cacheable(value = "role")
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
  @CacheEvict(value = "role", allEntries = true, beforeInvocation = true)
  public AppRoleEntity updateAppRole(final int id, final AppRoleRequest appRoleRequest) {
    log.debug("Update App Role: [{}], [{}]", id, appRoleRequest);
    final AppRoleEntity appRoleEntity = readAppRole(id);
    BeanUtils.copyProperties(appRoleRequest, appRoleEntity);
    return appRoleRepository.save(appRoleEntity);
  }

  // DELETE
  @CacheEvict(value = "role", allEntries = true, beforeInvocation = true)
  public AppRoleEntity softDeleteAppRole(final int id) {
    log.info("Soft Delete App Role: [{}]", id);
    final AppRoleEntity appRoleEntity = readAppRole(id);
    appRoleEntity.setDeletedDate(LocalDateTime.now());
    return appRoleRepository.save(appRoleEntity);
  }

  @CacheEvict(value = "role", allEntries = true, beforeInvocation = true)
  @Transactional
  public void hardDeleteAppRole(final int id) {
    log.info("Hard Delete App Role: [{}]", id);
    final AppRoleEntity appRoleEntity = readAppRole(id);
    appRoleRepository.delete(appRoleEntity);
  }

  // RESTORE
  @CacheEvict(value = "role", allEntries = true, beforeInvocation = true)
  public AppRoleEntity restoreSoftDeletedAppRole(final int id) {
    log.info("Restore Soft Deleted App Role: [{}]", id);
    final AppRoleEntity appRoleEntity = readAppRole(id);
    appRoleEntity.setDeletedDate(null);
    return appRoleRepository.save(appRoleEntity);
  }
}
