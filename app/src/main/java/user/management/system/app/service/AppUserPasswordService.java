package user.management.system.app.service;

import static user.management.system.app.util.JwtUtils.decodeEmailAddress;

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
      final AppUserService appUserService, final PasswordUtils passwordUtils) {
    this.appUserService = appUserService;
    this.passwordUtils = passwordUtils;
  }

  public AppUserEntity loginUser(final UserLoginRequest userLoginRequest) {
    final AppUserEntity appUserEntity =
        appUserService.readAppUser(userLoginRequest.getApp(), userLoginRequest.getEmail());

    if (!appUserEntity.getIsValidated()) {
      throw new UserNotValidatedException();
    }

    boolean isLoginSuccess =
        passwordUtils.verifyPassword(userLoginRequest.getPassword(), appUserEntity.getPassword());

    if (!isLoginSuccess) {
      throw new UserNotAuthorizedException();
    }

    return appUserEntity;
  }

  public void resetUser(final UserLoginRequest userLoginRequest) {
    final AppUserEntity appUserEntity =
        appUserService.readAppUser(userLoginRequest.getApp(), userLoginRequest.getEmail());
    appUserEntity.setPassword(passwordUtils.hashPassword(userLoginRequest.getPassword()));
    appUserService.updateAppUser(appUserEntity);
  }

  public String validateAndResetUser(
      final String app, final String encodedEmail, final boolean isValidate) {
    final String email = decodeEmailAddress(encodedEmail);
    final AppUserEntity appUserEntity = appUserService.readAppUser(app, email);

    if (isValidate) {
      appUserEntity.setIsValidated(true);
      appUserService.updateAppUser(appUserEntity);
    }

    return email;
  }
}
