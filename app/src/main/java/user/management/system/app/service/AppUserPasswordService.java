package user.management.system.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.UserNotAuthorizedException;
import user.management.system.app.exception.UserNotValidatedException;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.util.PasswordUtils;

@Slf4j
@Service
public class AppUserPasswordService {

  private final AppUserService appUserService;
  private final PasswordUtils passwordUtils;

  public AppUserPasswordService(
      final AppUserService appUserService,
      final PasswordUtils passwordUtils) {
    this.appUserService = appUserService;
    this.passwordUtils = passwordUtils;
  }

  // LOGIN
  public AppUserEntity loginUser(final UserLoginRequest userLoginRequest) {
    AppUserEntity appUserEntity = appUserService.readAppUser(userLoginRequest.getApp(), userLoginRequest.getEmail());

    if (!appUserEntity.getIsValidated()) {
      throw new UserNotValidatedException();
    }

    boolean isLoginSuccess = passwordUtils.verifyPassword(userLoginRequest.getPassword(), appUserEntity.getPassword());

    if (!isLoginSuccess) {
      throw new UserNotAuthorizedException();
    }

    return appUserEntity;
  }
}
