package auth.service.app.service;

import static auth.service.app.util.JwtUtils.decodeEmailAddress;

import auth.service.app.exception.ElementNotActiveException;
import auth.service.app.exception.UserNotActiveException;
import auth.service.app.exception.UserNotAuthorizedException;
import auth.service.app.exception.UserNotValidatedException;
import auth.service.app.model.dto.UserLoginRequest;
import auth.service.app.model.dto.UserLoginResponse;
import auth.service.app.model.entity.AppUserEntity;
import auth.service.app.model.entity.AppsAppUserEntity;
import auth.service.app.model.entity.AppsEntity;
import auth.service.app.model.enums.StatusEnums;
import auth.service.app.util.PasswordUtils;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserPasswordService {

  private final AppsAppUserService appsAppUserService;
  private final AppUserService appUserService;
  private final PasswordUtils passwordUtils;
  private final AppTokenService appTokenService;

  @Transactional
  public UserLoginResponse loginUser(final String appId, final UserLoginRequest userLoginRequest) {
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

    final boolean isLoginSuccess =
        passwordUtils.verifyPassword(userLoginRequest.getPassword(), appUserEntity.getPassword());

    if (!isLoginSuccess) {
      throw new UserNotAuthorizedException();
    }

    return appTokenService.saveToken(null, null, appUserEntity, appId);
  }

  public AppUserEntity resetUser(final String appId, final UserLoginRequest userLoginRequest) {
    final AppsAppUserEntity appsAppUserEntity =
        appsAppUserService.readAppsAppUser(appId, userLoginRequest.getEmail());
    final AppUserEntity appUserEntity = appsAppUserEntity.getAppUser();
    appUserEntity.setPassword(passwordUtils.hashPassword(userLoginRequest.getPassword()));
    return appUserService.updateAppUser(appUserEntity);
  }

  public AppUserEntity validateAndResetUser(
      final String appId, final String encodedEmail, final boolean isValidate) {
    final String email = decodeEmailAddress(encodedEmail);
    final AppsAppUserEntity appsAppUserEntity = appsAppUserService.readAppsAppUser(appId, email);
    final AppUserEntity appUserEntity = appsAppUserEntity.getAppUser();

    if (isValidate) {
      appUserEntity.setIsValidated(true);
      appUserService.updateAppUser(appUserEntity);
    }

    return appUserEntity;
  }
}
