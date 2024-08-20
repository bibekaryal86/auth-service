package user.management.system.app.service;

import static user.management.system.app.util.JwtUtils.decodeEmailAddress;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotActiveException;
import user.management.system.app.exception.UserNotActiveException;
import user.management.system.app.exception.UserNotAuthorizedException;
import user.management.system.app.exception.UserNotValidatedException;
import user.management.system.app.model.dto.UserLoginRequest;
import user.management.system.app.model.entity.AppUserEntity;
import user.management.system.app.model.entity.AppsAppUserEntity;
import user.management.system.app.model.entity.AppsEntity;
import user.management.system.app.model.enums.StatusEnums;
import user.management.system.app.util.PasswordUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserPasswordService {

  private final AppsAppUserService appsAppUserService;
  private final AppUserService appUserService;
  private final PasswordUtils passwordUtils;

  public AppUserEntity loginUser(final String appId, final UserLoginRequest userLoginRequest) {
    final AppsAppUserEntity appsAppUserEntity =
        appsAppUserService.readAppsAppUser(appId, userLoginRequest.getEmail());
    final AppsEntity appsEntity = appsAppUserEntity.getApp();
    final AppUserEntity appUserEntity = appsAppUserEntity.getAppUser();

    if (appsEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("App", appId);
    }

    if (appUserEntity.getDeletedDate() != null) {
      throw new ElementNotActiveException("User", userLoginRequest.getEmail());
    }

    if (!appUserEntity.getIsValidated()) {
      throw new UserNotValidatedException();
    }

    if (!Objects.equals(appUserEntity.getStatus(), StatusEnums.AppUserStatus.ACTIVE.toString())) {
      throw new UserNotActiveException();
    }

    boolean isLoginSuccess =
        passwordUtils.verifyPassword(userLoginRequest.getPassword(), appUserEntity.getPassword());

    if (!isLoginSuccess) {
      throw new UserNotAuthorizedException();
    }

    return appUserEntity;
  }

  public void resetUser(final String appId, final UserLoginRequest userLoginRequest) {
    final AppsAppUserEntity appsAppUserEntity =
        appsAppUserService.readAppsAppUser(appId, userLoginRequest.getEmail());
    final AppUserEntity appUserEntity = appsAppUserEntity.getAppUser();
    appUserEntity.setPassword(passwordUtils.hashPassword(userLoginRequest.getPassword()));
    appUserService.updateAppUser(appUserEntity);
  }

  public String validateAndResetUser(
      final String appId, final String encodedEmail, final boolean isValidate) {
    final String email = decodeEmailAddress(encodedEmail);
    final AppsAppUserEntity appsAppUserEntity = appsAppUserService.readAppsAppUser(appId, email);
    final AppUserEntity appUserEntity = appsAppUserEntity.getAppUser();

    if (isValidate) {
      appUserEntity.setIsValidated(true);
      appUserService.updateAppUser(appUserEntity);
    }

    return email;
  }
}
