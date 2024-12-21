package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.StatusTypeRequest;
import auth.service.app.model.entity.StatusTypeEntity;
import auth.service.app.repository.StatusTypeRepository;
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
public class StatusTypeService {

  private final StatusTypeRepository statusTypeRepository;

  // CREATE
  @CacheEvict(value = "status_types", allEntries = true, beforeInvocation = true)
  public StatusTypeEntity createStatusType(final StatusTypeRequest statusTypeRequest) {
    log.debug("Create Status Type: [{}]", statusTypeRequest);
    StatusTypeEntity statusTypeEntity = new StatusTypeEntity();
    BeanUtils.copyProperties(statusTypeRequest, statusTypeEntity);
    return statusTypeRepository.save(statusTypeEntity);
  }

  // READ
  @Cacheable(value = "status_types")
  public List<StatusTypeEntity> readStatusTypes() {
    log.debug("Read Status Types...");
    return statusTypeRepository.findAll(
        Sort.by(Sort.Order.asc("componentName"), Sort.Order.asc("statusName")));
  }

  /** Use {@link CircularDependencyService#readStatusType(Long)} */
  public StatusTypeEntity readStatusType(final Long id) {
    log.debug("Read Status Type: [{}]", id);
    return statusTypeRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Status Type", String.valueOf(id)));
  }

  // UPDATE
  @CacheEvict(value = "status_types", allEntries = true, beforeInvocation = true)
  public StatusTypeEntity updateStatusType(
      final Long id, final StatusTypeRequest statusTypeRequest) {
    log.debug("Update Status Type: [{}], [{}]", id, statusTypeRequest);
    final StatusTypeEntity statusTypeEntity = readStatusType(id);
    BeanUtils.copyProperties(statusTypeRequest, statusTypeEntity);
    return statusTypeRepository.save(statusTypeEntity);
  }

  // DELETE
  @CacheEvict(value = "status_types", allEntries = true, beforeInvocation = true)
  public StatusTypeEntity softDeleteStatusType(final Long id) {
    log.info("Soft Delete Status Type: [{}]", id);
    final StatusTypeEntity statusTypeEntity = readStatusType(id);
    statusTypeEntity.setDeletedDate(LocalDateTime.now());
    return statusTypeRepository.save(statusTypeEntity);
  }

  @CacheEvict(value = "status_types", allEntries = true, beforeInvocation = true)
  @Transactional
  public void hardDeleteStatusType(final Long id) {
    log.info("Hard Delete Status Type: [{}]", id);
    final StatusTypeEntity statusTypeEntity = readStatusType(id);
    statusTypeRepository.delete(statusTypeEntity);
  }

  // RESTORE
  @CacheEvict(value = "status_types", allEntries = true, beforeInvocation = true)
  public StatusTypeEntity restoreSoftDeletedStatusType(final Long id) {
    log.info("Restore Soft Deleted Status Type: [{}]", id);
    final StatusTypeEntity statusTypeEntity = readStatusType(id);
    statusTypeEntity.setDeletedDate(null);
    return statusTypeRepository.save(statusTypeEntity);
  }
}
