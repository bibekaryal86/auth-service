package user.management.system.app.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.UserStatusRequest;
import user.management.system.app.model.entity.UserStatusEntity;
import user.management.system.app.repository.UserStatusRepository;

@Service
@Slf4j
public class UserStatusService {
  private final UserStatusRepository userStatusRepository;

  public UserStatusService(final UserStatusRepository userStatusRepository) {
    this.userStatusRepository = userStatusRepository;
  }

  public UserStatusEntity createUserStatus(final UserStatusRequest userStatusRequest) {
    log.debug("Create User Status: [{}]", userStatusRequest);
    UserStatusEntity userStatusEntity = new UserStatusEntity();
    BeanUtils.copyProperties(userStatusRequest, userStatusEntity);
    return userStatusRepository.save(userStatusEntity);
  }

  public List<UserStatusEntity> retrieveAllUserStatuses() {
    log.debug("Retrieve All User Statuses...");
    return userStatusRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
  }

  public UserStatusEntity retrieveUserStatusById(final int id) {
    log.debug("Retrieve User Status By Id: [{}]", id);
    return userStatusRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("User Status", id));
  }

  public UserStatusEntity updateUserStatus(
      final int id, final UserStatusRequest userStatusRequest) {
    log.debug("Update User Status: [{}], [{}]", id, userStatusRequest);
    // throws exception if trying to update anything that doesn't exist
    UserStatusEntity userStatusEntity = retrieveUserStatusById(id);
    BeanUtils.copyProperties(userStatusRequest, userStatusEntity);
    return userStatusRepository.save(userStatusEntity);
  }

  public void deleteUserStatus(final int id) {
    log.info("Delete User Status: [{}]", id);
    // throws exception if trying to delete anything that doesn't exist
    UserStatusEntity userStatusEntity = retrieveUserStatusById(id);
    userStatusRepository.delete(userStatusEntity);
  }
}
