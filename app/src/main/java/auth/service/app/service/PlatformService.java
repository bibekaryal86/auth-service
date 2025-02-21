package auth.service.app.service;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.PlatformRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.repository.PlatformRepository;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.JpaDataUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformService {

  private final PlatformRepository platformRepository;

  // CREATE
  public PlatformEntity createPlatform(final PlatformRequest platformRequest) {
    log.debug("Create Platform: [{}]", platformRequest);
    PlatformEntity platformEntity = new PlatformEntity();
    BeanUtils.copyProperties(platformRequest, platformEntity);
    return platformRepository.save(platformEntity);
  }

  // READ
  public Page<PlatformEntity> readPlatforms(final RequestMetadata requestMetadata) {
    log.debug("Read Platforms: [{}]", requestMetadata);

    final RequestMetadata requestMetadataToUse =
        CommonUtils.isRequestMetadataIncluded(requestMetadata)
            ? requestMetadata
            : CommonUtils.defaultRequestMetadata("platformName");
    final Pageable pageable = JpaDataUtils.getQueryPageable(requestMetadataToUse, "platformName");
    final Specification<PlatformEntity> specification =
        JpaDataUtils.getQuerySpecification(requestMetadataToUse);
    return platformRepository.findAll(specification, pageable);
  }

  /** Use {@link CircularDependencyService#readPlatform(Long, boolean)} */
  private PlatformEntity readPlatform(final Long id) {
    log.debug("Read Platform: [{}]", id);
    return platformRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Platform", String.valueOf(id)));
  }

  // UPDATE
  public PlatformEntity updatePlatform(final Long id, final PlatformRequest platformRequest) {
    log.debug("Update Platform: [{}], [{}]", id, platformRequest);
    final PlatformEntity platformEntity = readPlatform(id);

    if (platformEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Platform", String.valueOf(id));
    }

    BeanUtils.copyProperties(platformRequest, platformEntity);
    return platformRepository.save(platformEntity);
  }

  // DELETE
  public PlatformEntity softDeletePlatform(final Long id) {
    log.info("Soft Delete Platform: [{}]", id);
    final PlatformEntity platformEntity = readPlatform(id);

    if (platformEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("Platform", String.valueOf(id));
    }

    platformEntity.setDeletedDate(LocalDateTime.now());
    return platformRepository.save(platformEntity);
  }

  public void hardDeletePlatform(final Long id) {
    log.info("Hard Delete Platform: [{}]", id);
    final PlatformEntity platformEntity = readPlatform(id);
    platformRepository.delete(platformEntity);
  }

  // RESTORE
  public PlatformEntity restoreSoftDeletedPlatform(final Long id) {
    log.info("Restore Soft Deleted Platform: [{}]", id);
    final PlatformEntity platformEntity = readPlatform(id);
    platformEntity.setDeletedDate(null);
    return platformRepository.save(platformEntity);
  }
}
