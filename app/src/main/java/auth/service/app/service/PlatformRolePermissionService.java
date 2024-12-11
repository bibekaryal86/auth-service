package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformRolePermissionRequest;
import auth.service.app.model.entity.PermissionEntity;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.PlatformRolePermissionEntity;
import auth.service.app.model.entity.PlatformRolePermissionId;
import auth.service.app.model.entity.RoleEntity;
import auth.service.app.repository.PlatformRolePermissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformRolePermissionService {

  private final PlatformRolePermissionRepository platformRolePermissionRepository;
  private final ReadFromCacheService readFromCacheService;

  // CREATE
  public PlatformRolePermissionEntity createPlatformRolePermission(
      final PlatformRolePermissionRequest platformRolePermissionRequest) {
    log.debug("Create Platform Role Permission: [{}]", platformRolePermissionRequest);
    PlatformRolePermissionEntity platformRolePermissionEntity = new PlatformRolePermissionEntity();
    final PlatformEntity platformEntity =
        readFromCacheService.readPlatform(platformRolePermissionRequest.getPlatformId());
    final RoleEntity roleEntity =
        readFromCacheService.readRole(platformRolePermissionRequest.getRoleId());
    final PermissionEntity permissionEntity =
        readFromCacheService.readPermission(platformRolePermissionRequest.getPermissionId());
    platformRolePermissionEntity.setPlatform(platformEntity);
    platformRolePermissionEntity.setRole(roleEntity);
    platformRolePermissionEntity.setPermission(permissionEntity);
    platformRolePermissionEntity.setAssignedDate(LocalDateTime.now());
    platformRolePermissionEntity.setId(
        new PlatformRolePermissionId(
            platformEntity.getId(), roleEntity.getId(), permissionEntity.getId()));
    return platformRolePermissionRepository.save(platformRolePermissionEntity);
  }

  // READ
  public List<PlatformRolePermissionEntity> readPlatformRolePermissions() {
    log.debug("Read Platform Role Permissions...");
    return platformRolePermissionRepository.findAll(
        Sort.by(
            Sort.Order.asc("platform.platformName"),
            Sort.Order.asc("role.roleName"),
            Sort.Order.asc("permission.permissionName")));
  }

  public PlatformRolePermissionEntity readPlatformRolePermission(
      final Long platformId, final Long roleId, final Long permissionId) {
    log.info("Read Platform Role Permission: [{}], [{}], [{}]", platformId, roleId, permissionId);
    return platformRolePermissionRepository
        .findById(new PlatformRolePermissionId(platformId, roleId, permissionId))
        .orElseThrow(
            () ->
                new ElementNotFoundException(
                    "Platform Role Permission",
                    String.format("%s,%s,%s", platformId, roleId, permissionId)));
  }

  // UPDATE
  // not provided

  // DELETE
  @Transactional
  public void deletePlatformRolePermission(
      final Long platformId, final Long roleId, final Long permissionId) {
    log.info("Delete Platform Role Permission: [{}], [{}], [{}]", platformId, roleId, permissionId);
    final PlatformRolePermissionEntity platformRolePermissionEntity =
        readPlatformRolePermission(platformId, roleId, permissionId);
    platformRolePermissionRepository.delete(platformRolePermissionEntity);
  }
}
