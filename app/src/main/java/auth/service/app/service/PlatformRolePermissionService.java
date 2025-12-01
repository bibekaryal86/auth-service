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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformRolePermissionService {

  private final PlatformRolePermissionRepository platformRolePermissionRepository;
  private final CircularDependencyService circularDependencyService;

  // ASSIGN
  public PlatformRolePermissionEntity assignPlatformRolePermission(
      final PlatformRolePermissionRequest platformRolePermissionRequest) {
    log.debug("Assign Platform Role Permission: [{}]", platformRolePermissionRequest);
    PlatformRolePermissionEntity platformRolePermissionEntity = new PlatformRolePermissionEntity();
    final PlatformEntity platformEntity =
        circularDependencyService.readPlatform(
            platformRolePermissionRequest.getPlatformId(), Boolean.FALSE);
    final PermissionEntity permissionEntity =
        circularDependencyService.readPermission(
            platformRolePermissionRequest.getPermissionId(), Boolean.FALSE);
    final RoleEntity roleEntity =
        circularDependencyService.readRole(
            platformRolePermissionRequest.getRoleId(), Boolean.FALSE);

    platformRolePermissionEntity.setPlatform(platformEntity);
    platformRolePermissionEntity.setPermission(permissionEntity);
    platformRolePermissionEntity.setRole(roleEntity);
    platformRolePermissionEntity.setAssignedDate(LocalDateTime.now());
    platformRolePermissionEntity.setUnassignedDate(null);
    platformRolePermissionEntity.setId(
        new PlatformRolePermissionId(
            platformEntity.getId(), roleEntity.getId(), permissionEntity.getId()));

    return platformRolePermissionRepository.save(platformRolePermissionEntity);
  }

  // UNASSIGN
  public PlatformRolePermissionEntity unassignPlatformRolePermission(
      final Long platformId, final Long roleId, final Long permissionId) {
    log.info(
        "Unassign Platform Role Permission: [{}], [{}], [{}]", platformId, roleId, permissionId);
    final PlatformRolePermissionEntity platformRolePermissionEntity =
        readPlatformRolePermission(platformId, roleId, permissionId);
    platformRolePermissionEntity.setUnassignedDate(LocalDateTime.now());
    return platformRolePermissionRepository.save(platformRolePermissionEntity);
  }

  // READ
  private PlatformRolePermissionEntity readPlatformRolePermission(
      final Long platformId, final Long roleId, final Long permissionId) {
    log.debug("Read Platform Role Permission: [{}], [{}], [{}]", platformId, roleId, permissionId);
    return platformRolePermissionRepository
        .findById(new PlatformRolePermissionId(platformId, roleId, permissionId))
        .orElseThrow(
            () ->
                new ElementNotFoundException(
                    "Platform Role Permission",
                    String.format("%s,%s,%s", platformId, roleId, permissionId)));
  }

  public List<PlatformRolePermissionEntity> readPlatformRolePermissionsByPlatformIds(
      final List<Long> platformIds, final boolean isIncludeUnassigned) {
    log.debug(
        "Read Platform Role Permissions By Platform Ids: [{}] | [{}]",
        platformIds,
        isIncludeUnassigned);
    return platformRolePermissionRepository.findByPlatformIds(platformIds, isIncludeUnassigned);
  }

  public List<PlatformRolePermissionEntity> readPlatformRolePermissionsByPermissionIds(
      final List<Long> permissionIds, final boolean isIncludeUnassigned) {
    log.debug(
        "Read Platform Role Permissions By Permission Ids: [{}] | [{}]",
        permissionIds,
        isIncludeUnassigned);
    return platformRolePermissionRepository.findByPermissionIds(permissionIds, isIncludeUnassigned);
  }

  public List<PlatformRolePermissionEntity> readPlatformRolePermissionsByRoleIds(
      final List<Long> roleIds, final boolean isIncludeUnassigned) {
    log.debug(
        "Read Platform Role Permissions By Role Ids: [{}] | [{}]", roleIds, isIncludeUnassigned);
    return platformRolePermissionRepository.findByRoleIds(roleIds, isIncludeUnassigned);
  }

  public void hardDeletePlatformRolePermissionsByPlatformIds(final List<Long> platformIds) {
    log.info("Hard Delete Platform Role Permissions By Platform Ids: [{}]", platformIds);
    platformRolePermissionRepository.deleteByIdPlatformIdIn(platformIds);
  }

  public void hardDeletePlatformRolePermissionsByRoleIds(final List<Long> roleIds) {
    log.info("Hard Delete Platform Role Permissions By Role Ids: [{}]", roleIds);
    platformRolePermissionRepository.deleteByIdRoleIdIn(roleIds);
  }

  public void hardDeletePlatformRolePermissionsByProfileIds(final List<Long> permissionIds) {
    log.info("Hard Delete Platform Role Permissions By Permission Ids: [{}]", permissionIds);
    platformRolePermissionRepository.deleteByIdPermissionIdIn(permissionIds);
  }

  public void hardDeletePlatformRolePermission(
      final Long platformId, final Long roleId, final Long permissionId) {
    log.info(
        "Hard Delete Platform Role Permission: [{}], [{}], [{}]", platformId, roleId, permissionId);
    // will throw exception if not found
    readPlatformRolePermission(platformId, roleId, permissionId);
    platformRolePermissionRepository.deleteById(
        new PlatformRolePermissionId(platformId, roleId, permissionId));
  }
}
