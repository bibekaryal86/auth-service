package user.management.system.app.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.AddressTypeRequest;
import user.management.system.app.model.entity.AddressTypeEntity;
import user.management.system.app.repository.AddressTypeRepository;

@Service
@Slf4j
public class AddressTypeService {
  private final AddressTypeRepository addressTypeRepository;

  public AddressTypeService(final AddressTypeRepository addressTypeRepository) {
    this.addressTypeRepository = addressTypeRepository;
  }

  @CacheEvict(value = "addressTypes", allEntries = true, beforeInvocation = true)
  public AddressTypeEntity createAddressType(final AddressTypeRequest addressTypeRequest) {
    log.debug("Create Address Type: [{}]", addressTypeRequest);
    AddressTypeEntity addressTypeEntity = new AddressTypeEntity();
    BeanUtils.copyProperties(addressTypeRequest, addressTypeEntity);
    return addressTypeRepository.save(addressTypeEntity);
  }

  @Cacheable(value = "addressTypes")
  public List<AddressTypeEntity> retrieveAddressTypes() {
    log.debug("Retrieve Address Types...");
    return addressTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
  }

  public AddressTypeEntity retrieveAddressTypeById(final int id) {
    log.debug("Retrieve Address Type By Id: [{}]", id);
    return addressTypeRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Address Type", String.valueOf(id)));
  }

  @CacheEvict(value = "addressTypes", allEntries = true, beforeInvocation = true)
  public AddressTypeEntity updateAddressType(
      final int id, final AddressTypeRequest addressTypeRequest) {
    log.debug("Update Address Type: [{}], [{}]", id, addressTypeRequest);
    AddressTypeEntity addressTypeEntity = retrieveAddressTypeById(id);
    BeanUtils.copyProperties(addressTypeRequest, addressTypeEntity);
    return addressTypeRepository.save(addressTypeEntity);
  }

  @CacheEvict(value = "addressTypes", allEntries = true, beforeInvocation = true)
  public void deleteAddressType(final int id) {
    log.info("Delete Address Type: [{}]", id);
    AddressTypeEntity addressTypeEntity = retrieveAddressTypeById(id);
    addressTypeRepository.delete(addressTypeEntity);
  }
}
