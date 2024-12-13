package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.repository.PlatformRepository;
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
public class PlatformService {

  private final PlatformRepository platformRepository;

  // CREATE
  @CacheEvict(value = "platforms", allEntries = true, beforeInvocation = true)
  public PlatformEntity createPlatform(final PlatformRequest platformRequest) {
    log.debug("Create Platform: [{}]", platformRequest);
    PlatformEntity platformEntity = new PlatformEntity();
    BeanUtils.copyProperties(platformRequest, platformEntity);
    return platformRepository.save(platformEntity);
  }

  // READ
  @Cacheable(value = "platforms")
  public List<PlatformEntity> readPlatforms() {
    log.debug("Read Platforms...");
    return platformRepository.findAll(Sort.by(Sort.Direction.ASC, "platformName"));
  }

  /** Use {@link ReadFromCacheService#readPlatform(Long)} */
  public PlatformEntity readPlatform(final Long id) {
    log.debug("Read Platform: [{}]", id);
    return platformRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Platform", String.valueOf(id)));
  }

  // UPDATE
  @CacheEvict(value = "platforms", allEntries = true, beforeInvocation = true)
  public PlatformEntity updatePlatform(final Long id, final PlatformRequest platformRequest) {
    log.debug("Update Platform: [{}], [{}]", id, platformRequest);
    final PlatformEntity platformEntity = readPlatform(id);
    BeanUtils.copyProperties(platformRequest, platformEntity);
    return platformRepository.save(platformEntity);
  }

  // DELETE
  @CacheEvict(value = "platforms", allEntries = true, beforeInvocation = true)
  public PlatformEntity softDeletePlatform(final Long id) {
    log.info("Soft Delete Platform: [{}]", id);
    final PlatformEntity platformEntity = readPlatform(id);
    platformEntity.setDeletedDate(LocalDateTime.now());
    return platformRepository.save(platformEntity);
  }

  @CacheEvict(value = "platforms", allEntries = true, beforeInvocation = true)
  @Transactional
  public void hardDeletePlatform(final Long id) {
    log.info("Hard Delete Platform: [{}]", id);
    final PlatformEntity platformEntity = readPlatform(id);
    platformRepository.delete(platformEntity);
  }

  // RESTORE
  @CacheEvict(value = "platforms", allEntries = true, beforeInvocation = true)
  public PlatformEntity restoreSoftDeletedPlatform(final Long id) {
    log.info("Restore Soft Deleted Platform: [{}]", id);
    final PlatformEntity platformEntity = readPlatform(id);
    platformEntity.setDeletedDate(null);
    return platformRepository.save(platformEntity);
  }
}
