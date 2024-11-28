package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppsAppUserRequest;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsAppUserId;
import auth.service.app.model.entity.AppsEntity;
import auth.service.app.repository.AppsAppUserRepository;
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
public class AppsAppUserService {

  private final AppsAppUserRepository appsAppUserRepository;
  private final AppsService appsService;
  private final AppUserService appUserService;

  // CREATE
  public AppsAppUserEntity createAppsAppUser(final AppsAppUserRequest appsAppUserRequest) {
    log.debug("Create Apps App User: [{}]", appsAppUserRequest);
    AppsAppUserEntity appsAppUserEntity = new AppsAppUserEntity();
    final AppsEntity appsEntity = appsService.readApp(appsAppUserRequest.getAppId());
    final AppUserEntity appUserEntity = appUserService.readAppUser(appsAppUserRequest.getUserId());
    appsAppUserEntity.setApp(appsEntity);
    appsAppUserEntity.setAppUser(appUserEntity);
    appsAppUserEntity.setAssignedDate(LocalDateTime.now());
    appsAppUserEntity.setId(new AppsAppUserId(appsEntity.getId(), appUserEntity.getId()));
    return appsAppUserRepository.save(appsAppUserEntity);
  }

  // READ
  public List<AppsAppUserEntity> readAppsAppUsers() {
    log.debug("Read Apps App Users...");
    return appsAppUserRepository.findAll(
        Sort.by(Sort.Direction.ASC, "app.name", "appUser.lastName"));
  }

  public List<AppsAppUserEntity> readAppsAppUsersByAppId(final String appId) {
    log.debug("Read Apps App Users: [{}]", appId);
    return appsAppUserRepository.findAllByAppIdOrderByAppUserLastNameDesc(appId);
  }

  public List<AppsAppUserEntity> readAppsAppUsersByUserId(final int appUserId) {
    log.debug("Read Apps App Users: [{}]", appUserId);
    return appsAppUserRepository.findAllByAppUserIdOrderByAppNameAsc(appUserId);
  }

  public AppsAppUserEntity readAppsAppUser(final String appId, final String appUserEmail) {
    log.debug("Read Apps App User: [{}], [{}]", appId, appUserEmail);
    return appsAppUserRepository
        .findByAppIdAndAppUserEmail(appId, appUserEmail)
        .orElseThrow(
            () ->
                new ElementNotFoundException(
                    "Apps App User", String.format("%s,%s", appId, appUserEmail)));
  }

  // UPDATE
  // not provided

  // DELETE
  @Transactional
  public void deleteAppsAppUser(final String appId, final String appUserEmail) {
    log.info("Delete App Role Permission: [{}], [{}]", appId, appUserEmail);
    final AppsAppUserEntity appsAppUserEntity = readAppsAppUser(appId, appUserEmail);
    appsAppUserRepository.delete(appsAppUserEntity);
  }
}
