package auth.service.app.util;

import auth.service.app.exception.CheckPermissionException;
import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.token.AuthToken;
import java.util.List;
import java.util.Objects;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionCheck {

  @Before("@annotation(checkPermission)")
  public void checkPermission(final CheckPermission checkPermission) {
    final String[] requiredPermissions = checkPermission.value();

    try {
      final AuthToken authToken = CommonUtils.getAuthentication();
      final boolean isPermitted = checkUserPermission(authToken, List.of(requiredPermissions));

      if (!isPermitted) {
        throw new CheckPermissionException("Profile does not have required permissions...");
      }
    } catch (Exception ex) {
      if (ex instanceof CheckPermissionException) {
        throw ex;
      }
      throw new CheckPermissionException(ex.getMessage());
    }
  }

  public boolean checkPermissionDuplicate(final List<String> requiredPermissions) {
    try {
      final AuthToken authToken = CommonUtils.getAuthentication();
      return checkUserPermission(authToken, requiredPermissions);
    } catch (Exception ex) {
      if (ex instanceof CheckPermissionException) {
        throw ex;
      }
      throw new CheckPermissionException(ex.getMessage());
    }
  }

  public void checkProfileAccess(final String email, final long id) {
    try {
      final AuthToken authToken = CommonUtils.getAuthentication();
      final boolean isPermitted = checkUserIdEmail(email, id, authToken);

      if (!authToken.getIsSuperUser() && !isPermitted) {
        throw new CheckPermissionException(
            "Profile does not have required permissions to profile entity...");
      }
    } catch (Exception ex) {
      if (ex instanceof CheckPermissionException) {
        throw ex;
      }
      throw new CheckPermissionException(ex.getMessage());
    }
  }

  public List<ProfileEntity> filterProfileListByAccess(final List<ProfileEntity> profileEntities) {
    try {
      final AuthToken authToken = CommonUtils.getAuthentication();

      if (authToken.getIsSuperUser()) {
        return profileEntities;
      }

      return profileEntities.stream()
          .filter(profile -> checkUserIdEmail(profile.getEmail(), profile.getId(), authToken))
          .toList();
    } catch (Exception ex) {
      throw new CheckPermissionException(ex.getMessage());
    }
  }

  private boolean checkUserPermission(
      final AuthToken authToken, final List<String> requiredPermissions) {
    if (authToken.getIsSuperUser()) {
      return true;
    }

    return authToken.getPermissions().stream()
        .anyMatch(
            authTokenPermission ->
                requiredPermissions.contains(authTokenPermission.getPermissionName()));
  }

  private boolean checkUserIdEmail(final String email, final long id, final AuthToken authToken) {
    return Objects.equals(email, authToken.getProfile().getEmail())
        || Objects.equals(id, authToken.getProfile().getId());
  }
}
