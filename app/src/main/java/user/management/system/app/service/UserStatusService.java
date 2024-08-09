package user.management.system.app.service;

import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.EntityNotFoundException;
import user.management.system.app.model.dto.UserStatusRequest;
import user.management.system.app.model.entity.UserStatusEntity;
import user.management.system.app.repository.UserStatusRepository;

@Service
public class UserStatusService {
  private final UserStatusRepository userStatusRepository;

  public UserStatusService(final UserStatusRepository userStatusRepository) {
    this.userStatusRepository = userStatusRepository;
  }

  public UserStatusEntity createUserStatus(final UserStatusRequest userStatusRequest) {
    UserStatusEntity userStatusEntity = new UserStatusEntity();
    BeanUtils.copyProperties(userStatusRequest, userStatusEntity);
    return userStatusRepository.save(userStatusEntity);
  }

  public List<UserStatusEntity> retrieveAllUserStatuses() {
      List<UserStatusEntity> userStatusEntities = userStatusRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
      if (userStatusEntities.isEmpty()) {
          throw new EntityNotFoundException("User Status");
      } else {
          return userStatusEntities;
      }
  }

  public UserStatusEntity retrieveUserStatusById(final int id) {
    return userStatusRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User Status", id));
  }

  public UserStatusEntity updateUserStatus(
      final int id, final UserStatusRequest userStatusRequest) {
    UserStatusEntity userStatusEntity = retrieveUserStatusById(id);
    BeanUtils.copyProperties(userStatusRequest, userStatusEntity);
    return userStatusRepository.save(userStatusEntity);
  }
}
