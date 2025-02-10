package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AddressTypeRequest;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.repository.AddressTypeRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressTypeService {

  private final AddressTypeRepository addressTypeRepository;

  // CREATE
  @CacheEvict(value = "address_types", allEntries = true, beforeInvocation = true)
  public AddressTypeEntity createAddressType(final AddressTypeRequest addressTypeRequest) {
    log.debug("Create Address Type: [{}]", addressTypeRequest);
    AddressTypeEntity addressTypeEntity = new AddressTypeEntity();
    BeanUtils.copyProperties(addressTypeRequest, addressTypeEntity);
    return addressTypeRepository.save(addressTypeEntity);
  }

  // READ
  @Cacheable(value = "address_types")
  public List<AddressTypeEntity> readAddressTypes() {
    log.debug("Read Address Types...");
    return addressTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "typeName"));
  }

  /** Use {@link CircularDependencyService#readAddressType(Long)} */
  public AddressTypeEntity readAddressType(final Long id) {
    log.debug("Read Address Type: [{}]", id);
    return addressTypeRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Address Type", String.valueOf(id)));
  }

  // UPDATE
  @CacheEvict(value = "address_types", allEntries = true, beforeInvocation = true)
  public AddressTypeEntity updateAddressType(
      final Long id, final AddressTypeRequest addressTypeRequest) {
    log.debug("Update Address Type: [{}], [{}]", id, addressTypeRequest);
    final AddressTypeEntity addressTypeEntity = readAddressType(id);
    BeanUtils.copyProperties(addressTypeRequest, addressTypeEntity);
    return addressTypeRepository.save(addressTypeEntity);
  }

  // DELETE
  @CacheEvict(value = "address_types", allEntries = true, beforeInvocation = true)
  public AddressTypeEntity softDeleteAddressType(final Long id) {
    log.info("Soft Delete Address Type: [{}]", id);
    final AddressTypeEntity addressTypeEntity = readAddressType(id);
    addressTypeEntity.setDeletedDate(LocalDateTime.now());
    return addressTypeRepository.save(addressTypeEntity);
  }

  @CacheEvict(value = "address_types", allEntries = true, beforeInvocation = true)
  public void hardDeleteAddressType(final Long id) {
    log.info("Hard Delete Address Type: [{}]", id);
    final AddressTypeEntity addressTypeEntity = readAddressType(id);
    addressTypeRepository.delete(addressTypeEntity);
  }

  // RESTORE
  @CacheEvict(value = "address_types", allEntries = true, beforeInvocation = true)
  public AddressTypeEntity restoreSoftDeletedAddressType(final Long id) {
    log.info("Restore Soft Deleted Address Type: [{}]", id);
    final AddressTypeEntity addressTypeEntity = readAddressType(id);
    addressTypeEntity.setDeletedDate(null);
    return addressTypeRepository.save(addressTypeEntity);
  }
}
