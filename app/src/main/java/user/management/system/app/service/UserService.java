package user.management.system.app.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.UserRequest;
import user.management.system.app.model.entity.UserEntity;
import user.management.system.app.model.entity.UserStatusEntity;
import user.management.system.app.repository.UserRepository;
import user.management.system.app.util.PasswordUtils;

@Service
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final UserStatusService userStatusService;
  private final PasswordUtils passwordUtils;

  public UserService(
      final UserRepository userRepository,
      final UserStatusService userStatusService,
      final PasswordUtils passwordUtils) {
    this.userRepository = userRepository;
    this.userStatusService = userStatusService;
    this.passwordUtils = passwordUtils;
  }

  public UserEntity createUser(final UserRequest userRequest) {
    log.debug("Create User: [{}]", userRequest);
    UserStatusEntity status = userStatusService.retrieveUserStatusById(userRequest.getStatusId());
    UserEntity userEntity = new UserEntity();
    BeanUtils.copyProperties(userRequest, userEntity, "password", "statusId", "isGuestUser");
    userEntity.setPassword(passwordUtils.hashPassword(userRequest.getPassword()));
    userEntity.setStatus(status);
    userEntity.setIsValidated(false);
    // TODO assign role, send email
    return userRepository.save(userEntity);
  }

  public List<UserEntity> retrieveUsers() {
    log.debug("Retrieve Users...");
    return userRepository.findAll(Sort.by(Sort.Direction.ASC, "firstName"));
  }

  public UserEntity retrieveUserByEmail(final String email) {
    log.debug("Retrieve User by Email: [{}]", email);
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ElementNotFoundException("User", email));
  }

  public UserEntity retrieveUserById(final int id) {
    log.debug("Retrieve User by ID: [{}]", id);
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("User", String.valueOf(id)));
  }

  public UserEntity updateUser(final int id, final UserRequest userRequest) {
    log.debug("Update User: [{}], [{}]", id, userRequest);
    UserEntity userEntity = retrieveUserById(id);
    UserStatusEntity status = userStatusService.retrieveUserStatusById(userRequest.getStatusId());
    userEntity.setStatus(status);
    BeanUtils.copyProperties(userRequest, userEntity, "password", "statusId", "isGuestUser");
    return userRepository.save(userEntity);
  }

  public void deleteUser(final int id, final boolean isHardDelete) {
    log.info("Delete User: [{}], [{}]", id, isHardDelete);
    UserEntity userEntity = retrieveUserById(id);

    if (isHardDelete) {
      userRepository.delete(userEntity);
    } else {
      UserStatusEntity status = getUserStatusEntity("DELETED");
      userEntity.setStatus(status);
      userEntity.setDeletedDate(LocalDateTime.now());
    }
  }

  public void restoreUser(final int id) {
    log.info("Restore User: [{}]", id);
    UserEntity userEntity = retrieveUserById(id);
    userEntity.setDeletedDate(null);
    UserStatusEntity status = getUserStatusEntity("PENDING");
    userEntity.setStatus(status);
    // TODO send validation email
    userRepository.save(userEntity);
  }

  private UserStatusEntity getUserStatusEntity(final String userStatusName) {
    List<UserStatusEntity> userStatusEntities = userStatusService.retrieveUserStatuses();
    return userStatusEntities.stream()
        .filter(userStatusEntity -> userStatusName.equals(userStatusEntity.getName()))
        .findFirst()
        .orElseThrow(() -> new ElementNotFoundException("User Status", userStatusName));
  }
}
