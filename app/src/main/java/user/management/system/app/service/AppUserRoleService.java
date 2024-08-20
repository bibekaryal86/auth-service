package user.management.system.app.service;

import static user.management.system.app.util.ConstantUtils.APP_ROLE_NAME_GUEST;
import static user.management.system.app.util.ConstantUtils.APP_ROLE_NAME_STANDARD;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AppUserRoleRequest;
import user.management.system.app.model.entity.AppRoleEntity;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppUserRoleEntity;
import user.management.system.app.model.entity.AppUserRoleId;
import user.management.system.app.model.events.AppUserCreatedEvent;
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
    return createAppUserRoleEntity(appUserEntity, appRoleEntity);
  }

  @EventListener
  public void handleUserCreated(final AppUserCreatedEvent appUserCreatedEvent) {
    final AppUserEntity appUserEntity = appUserCreatedEvent.getAppUserEntity();
    final boolean isGuestUser = appUserCreatedEvent.isGuestUser();

    log.info(
        "Handle User Created: [{}], [{}]",
        appUserCreatedEvent.getAppsEntity().getName(),
        appUserEntity.getId());

    final List<AppRoleEntity> appRoleEntities = appRoleService.readAppRoles();
    final AppRoleEntity appRoleEntity =
        appRoleEntities.stream()
            .filter(
                are ->
                    isGuestUser
                        ? are.getName().equals(APP_ROLE_NAME_GUEST)
                        : are.getName().equals(APP_ROLE_NAME_STANDARD))
            .findFirst()
            .orElse(null);

    if (appRoleEntity == null) {
      log.error(
          "Handle User Created, App Role Entity is NULL: [{}], [{}]",
          appUserCreatedEvent.getAppsEntity().getName(),
          appUserEntity.getId());
    } else {
      createAppUserRoleEntity(appUserEntity, appRoleEntity);
    }
  }

  private AppUserRoleEntity createAppUserRoleEntity(
      final AppUserEntity appUserEntity, final AppRoleEntity appRoleEntity) {
    AppUserRoleEntity appUserRoleEntity = new AppUserRoleEntity();
    appUserRoleEntity.setAppUser(appUserEntity);
    appUserRoleEntity.setAppRole(appRoleEntity);
    appUserRoleEntity.setAssignedDate(LocalDateTime.now());
    return appUserRoleRepository.save(appUserRoleEntity);
  }

  // READ
  public List<AppUserRoleEntity> readAppUserRoles() {
    log.debug("Read App User Roles...");
    return appUserRoleRepository.findAll(Sort.by(Sort.Direction.ASC, "appUser.name"));
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
  public void deleteAppUserRole(final int appUserId, final int appRoleId) {
    log.info("Delete App User Role: [{}], [{}]", appUserId, appRoleId);
    final AppUserRoleEntity appUserRoleEntity = readAppUserRole(appUserId, appRoleId);
    appUserRoleRepository.delete(appUserRoleEntity);
  }
}
