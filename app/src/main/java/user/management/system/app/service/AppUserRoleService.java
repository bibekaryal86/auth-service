package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppUserRoleRequest;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppUserRoleId;
import user.management.system.app.repository.AppUserRoleRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserRoleService {

  private final AppUserRoleRepository appUserRoleRepository;
  private final AppUserService appUserService;
  private final AppRoleService appRoleService;

  // CREATE
  public AppUserRoleEntity createAppUserRole(final AppUserRoleRequest appUserRoleRequest) {
    log.debug("Create App User Role: [{}]", appUserRoleRequest);
    final AppUserEntity appUserEntity = appUserService.readAppUser(appUserRoleRequest.getUserId());
    final AppRoleEntity appRoleEntity = appRoleService.readAppRole(appUserRoleRequest.getRoleId());

    AppUserRoleEntity appUserRoleEntity = new AppUserRoleEntity();
    appUserRoleEntity.setAppUser(appUserEntity);
    appUserRoleEntity.setAppRole(appRoleEntity);
    appUserRoleEntity.setAssignedDate(LocalDateTime.now());
    appUserRoleEntity.setId(new AppUserRoleId(appUserEntity.getId(), appRoleEntity.getId()));
    return appUserRoleRepository.save(appUserRoleEntity);
  }

  // READ
  public List<AppUserRoleEntity> readAppUserRoles() {
    log.debug("Read App User Roles...");
    return appUserRoleRepository.findAll(Sort.by(Sort.Direction.ASC, "appUser.lastName"));
  }

  public List<AppUserRoleEntity> readAppUserRoles(final int appUserId) {
    log.debug("Read App User Roles: [{}]", appUserId);
    return appUserRoleRepository.findByIdAppUserIdOrderByAppRoleNameAsc(appUserId);
  }

  public List<AppUserRoleEntity> readAppUserRoles(final List<Integer> appUserIds) {
    log.debug("Read App User Roles: [{}]", appUserIds);
    return appUserRoleRepository.findByIdAppUserIdInOrderByAppRoleNameAsc(appUserIds);
  }

  public AppUserRoleEntity readAppUserRole(final int appUserId, final int appRoleId) {
    log.debug("Read App User Role: [{}], [{}]", appUserId, appRoleId);
    return appUserRoleRepository
        .findById(new AppUserRoleId(appUserId, appRoleId))
        .orElseThrow(
            () ->
                new ElementNotFoundException(
                    "App User Role", String.format("%s,%s", appUserId, appRoleId)));
  }

  // UPDATE
  // not provided

  // DELETE
  @Transactional
  public void deleteAppUserRole(final int appUserId, final int appRoleId) {
    log.info("Delete App User Role: [{}], [{}]", appUserId, appRoleId);
    final AppUserRoleEntity appUserRoleEntity = readAppUserRole(appUserId, appRoleId);
    appUserRoleRepository.delete(appUserRoleEntity);
  }
}
