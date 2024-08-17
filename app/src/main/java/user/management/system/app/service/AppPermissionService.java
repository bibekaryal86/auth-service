package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppPermissionRequest;
import user.management.system.app.model.entity.AppPermissionEntity;
import user.management.system.app.repository.AppPermissionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppPermissionService {

  private final AppPermissionRepository appPermissionRepository;

  // CREATE
  public AppPermissionEntity createAppPermission(final AppPermissionRequest appPermissionRequest) {
    log.debug("Create App Permission: [{}]", appPermissionRequest);
    AppPermissionEntity appPermissionEntity = new AppPermissionEntity();
    BeanUtils.copyProperties(appPermissionRequest, appPermissionEntity);
    return appPermissionRepository.save(appPermissionEntity);
  }

  // READ
  public List<AppPermissionEntity> readAppPermissions() {
    log.debug("Read App Permissions...");
    return appPermissionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
  }

  public List<AppPermissionEntity> readAppPermissions(final String app) {
    log.debug("Read App Permissions: [{}]", app);
    return appPermissionRepository.findByAppOrderByNameAsc(app);
  }

  public AppPermissionEntity readAppPermission(final int id) {
    log.debug("Read App Permission: [{}]", id);
    return appPermissionRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Permission", String.valueOf(id)));
  }

  // UPDATE
  public AppPermissionEntity updateAppPermission(
      final int id, final AppPermissionRequest appPermissionRequest) {
    log.debug("Update App Permission: [{}], [{}]", id, appPermissionRequest);
    final AppPermissionEntity appPermissionEntity = readAppPermission(id);
    BeanUtils.copyProperties(appPermissionRequest, appPermissionEntity);
    return appPermissionRepository.save(appPermissionEntity);
  }

  // DELETE
  public AppPermissionEntity softDeleteAppPermission(final int id) {
    log.info("Soft Delete App Permission: [{}]", id);
    final AppPermissionEntity appPermissionEntity = readAppPermission(id);
    appPermissionEntity.setDeletedDate(LocalDateTime.now());
    return appPermissionRepository.save(appPermissionEntity);
  }

  public void hardDeleteAppPermission(final int id) {
    log.info("Hard Delete App Permission: [{}]", id);
    final AppPermissionEntity appPermissionEntity = readAppPermission(id);
    appPermissionRepository.delete(appPermissionEntity);
  }

  // RESTORE
  public AppPermissionEntity restoreSoftDeletedAppPermission(final int id) {
    log.info("Restore Soft Deleted App Permission: [{}]", id);
    final AppPermissionEntity appPermissionEntity = readAppPermission(id);
    appPermissionEntity.setDeletedDate(null);
    return appPermissionRepository.save(appPermissionEntity);
  }
}
