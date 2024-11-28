package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AppsRequest;
import auth.service.app.model.entity.AppsEntity;
import auth.service.app.repository.AppsRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
public class AppsService {

  private final AppsRepository appsRepository;

  // CREATE
  @CacheEvict(value = "apps", allEntries = true, beforeInvocation = true)
  public AppsEntity createApp(final AppsRequest appsRequest) {
    log.debug("Create Apps: [{}]", appsRequest);
    AppsEntity appEntity = new AppsEntity();
    BeanUtils.copyProperties(appsRequest, appEntity);
    appEntity.setId(getRandomId());
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
  public AppsEntity updateApps(final String id, final AppsRequest appsRequest) {
    log.debug("Update Apps: [{}], [{}]", id, appsRequest);
    final AppsEntity appEntity = readApp(id);
    BeanUtils.copyProperties(appsRequest, appEntity);
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
  @Transactional
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

  /**
   * @return 8 character long random substring of a UUID
   */
  private String getRandomId() {
    try {
      UUID uuid = UUID.randomUUID();
      String uuidString = uuid.toString().replace("-", "");
      int maxStartIndex = uuidString.length() - 8;
      int startIndex = ThreadLocalRandom.current().nextInt(maxStartIndex + 1);
      return uuidString.substring(startIndex, startIndex + 8);
    } catch (Exception ex) {
      return String.valueOf(ThreadLocalRandom.current().nextInt(10000000, 100000000));
    }
  }
}
