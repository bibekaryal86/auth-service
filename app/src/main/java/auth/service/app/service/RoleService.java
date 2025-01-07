package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.RoleRequest;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.RoleRepository;
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
public class RoleService {

  private final RoleRepository roleRepository;

  // CREATE
  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public RoleEntity createRole(final RoleRequest roleRequest) {
    log.debug("Create Role: [{}]", roleRequest);
    RoleEntity roleEntity = new RoleEntity();
    BeanUtils.copyProperties(roleRequest, roleEntity);
    return roleRepository.save(roleEntity);
  }

  // READ
  @Cacheable(value = "roles")
  public List<RoleEntity> readRoles() {
    log.debug("Read Roles...");
    return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "roleName"));
  }

  /** Use {@link CircularDependencyService#readRole(Long)} */
  public RoleEntity readRole(final Long id) {
    log.debug("Read Role: [{}]", id);
    return roleRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Role", String.valueOf(id)));
  }

  // UPDATE
  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public RoleEntity updateRole(final Long id, final RoleRequest roleRequest) {
    log.debug("Update Role: [{}], [{}]", id, roleRequest);
    final RoleEntity roleEntity = readRole(id);
    BeanUtils.copyProperties(roleRequest, roleEntity);
    return roleRepository.save(roleEntity);
  }

  // DELETE
  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public RoleEntity softDeleteRole(final Long id) {
    log.info("Soft Delete Role: [{}]", id);
    final RoleEntity roleEntity = readRole(id);
    roleEntity.setDeletedDate(LocalDateTime.now());
    return roleRepository.save(roleEntity);
  }

  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  @Transactional
  public void hardDeleteRole(final Long id) {
    log.info("Hard Delete Role: [{}]", id);
    final RoleEntity roleEntity = readRole(id);
    roleRepository.delete(roleEntity);
  }

  // RESTORE
  @CacheEvict(value = "roles", allEntries = true, beforeInvocation = true)
  public RoleEntity restoreSoftDeletedRole(final Long id) {
    log.info("Restore Soft Deleted Role: [{}]", id);
    final RoleEntity roleEntity = readRole(id);
    roleEntity.setDeletedDate(null);
    return roleRepository.save(roleEntity);
  }
}
