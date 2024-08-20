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
import user.management.system.app.model.dto.AppsRequest;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.repository.AppsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppsService {

  private final AppsRepository appsRepository;

  // CREATE
  @CacheEvict(value = "apps", allEntries = true, beforeInvocation = true)
  public AppsEntity createApp(final AppsRequest appRequest) {
    log.debug("Create Apps: [{}]", appRequest);
    AppsEntity appEntity = new AppsEntity();
    BeanUtils.copyProperties(appRequest, appEntity);
    return appsRepository.save(appEntity);
  }

  // READ
  @Cacheable(value = "apps")
  public List<AppsEntity> readApps() {
    log.debug("Read Apps...");
    return appsRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
  }

  public AppsEntity readApp(final String id) {
    log.debug("Read App: [{}]", id);
    return appsRepository.findById(id).orElseThrow(() -> new ElementNotFoundException("App", id));
  }

  // UPDATE
  @CacheEvict(value = "apps", allEntries = true, beforeInvocation = true)
  public AppsEntity updateApps(final String id, final AppsRequest appRequest) {
    log.debug("Update Apps: [{}], [{}]", id, appRequest);
    final AppsEntity appEntity = readApp(id);
    BeanUtils.copyProperties(appRequest, appEntity);
    return appsRepository.save(appEntity);
  }

  // DELETE
  @CacheEvict(value = "apps", allEntries = true, beforeInvocation = true)
  public AppsEntity softDeleteApps(final String id) {
    log.info("Soft Delete Apps: [{}]", id);
    final AppsEntity appEntity = readApp(id);
    appEntity.setDeletedDate(LocalDateTime.now());
    return appsRepository.save(appEntity);
  }

  @CacheEvict(value = "apps", allEntries = true, beforeInvocation = true)
  public void hardDeleteApps(final String id) {
    log.info("Hard Delete Apps: [{}]", id);
    final AppsEntity appEntity = readApp(id);
    appsRepository.delete(appEntity);
  }

  // RESTORE
  @CacheEvict(value = "apps", allEntries = true, beforeInvocation = true)
  public AppsEntity restoreSoftDeletedApps(final String id) {
    log.info("Restore Soft Deleted Apps: [{}]", id);
    final AppsEntity appEntity = readApp(id);
    appEntity.setDeletedDate(null);
    return appsRepository.save(appEntity);
  }
}
