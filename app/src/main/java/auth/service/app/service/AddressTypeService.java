package auth.service.app.service;

import auth.service.app.exception.ElementNotFoundException;
import auth.service.app.model.dto.AddressTypeRequest;
import auth.service.app.model.dto.RequestMetadata;
import auth.service.app.model.entity.AddressTypeEntity;
import auth.service.app.repository.AddressTypeRepository;
import auth.service.app.util.CommonUtils;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressTypeService {

  private final AddressTypeRepository addressTypeRepository;

  // CREATE
  public AddressTypeEntity createAddressType(final AddressTypeRequest addressTypeRequest) {
    log.debug("Create Address Type: [{}]", addressTypeRequest);
    AddressTypeEntity addressTypeEntity = new AddressTypeEntity();
    BeanUtils.copyProperties(addressTypeRequest, addressTypeEntity);
    return addressTypeRepository.save(addressTypeEntity);
  }

  // READ
  public Page<AddressTypeEntity> readAddressTypes(final RequestMetadata requestMetadata) {
    log.debug("Read Address Types: [{}]", requestMetadata);
    if (CommonUtils.isRequestMetadataIncluded(requestMetadata)) {
      Specification<AddressTypeEntity> specification =
          CommonUtils.getQuerySpecification(requestMetadata);
      Pageable pageable = CommonUtils.getQueryPageable(requestMetadata, "typeName");
      return addressTypeRepository.findAll(specification, pageable);
    }
    return new PageImpl<>(addressTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "typeName")));
  }

  /** Use {@link CircularDependencyService#readAddressType(Long)} */
  public AddressTypeEntity readAddressType(final Long id) {
    log.debug("Read Address Type: [{}]", id);
    return addressTypeRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Address Type", String.valueOf(id)));
  }

  // UPDATE
  public AddressTypeEntity updateAddressType(
      final Long id, final AddressTypeRequest addressTypeRequest) {
    log.debug("Update Address Type: [{}], [{}]", id, addressTypeRequest);
    final AddressTypeEntity addressTypeEntity = readAddressType(id);
    BeanUtils.copyProperties(addressTypeRequest, addressTypeEntity);
    return addressTypeRepository.save(addressTypeEntity);
  }

  // DELETE
  public AddressTypeEntity softDeleteAddressType(final Long id) {
    log.info("Soft Delete Address Type: [{}]", id);
    final AddressTypeEntity addressTypeEntity = readAddressType(id);
    addressTypeEntity.setDeletedDate(LocalDateTime.now());
    return addressTypeRepository.save(addressTypeEntity);
  }

  public void hardDeleteAddressType(final Long id) {
    log.info("Hard Delete Address Type: [{}]", id);
    final AddressTypeEntity addressTypeEntity = readAddressType(id);
    addressTypeRepository.delete(addressTypeEntity);
  }

  // RESTORE
  public AddressTypeEntity restoreSoftDeletedAddressType(final Long id) {
    log.info("Restore Soft Deleted Address Type: [{}]", id);
    final AddressTypeEntity addressTypeEntity = readAddressType(id);
    addressTypeEntity.setDeletedDate(null);
    return addressTypeRepository.save(addressTypeEntity);
  }
}
