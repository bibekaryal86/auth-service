package auth.service.app.util;

import static auth.service.app.util.ConstantUtils.ROLE_NAME_SUPERUSER;

import auth.service.app.exception.CheckPermissionException;
import auth.service.app.model.annotation.CheckPermission;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.token.AuthToken;
import java.util.List;
import java.util.Objects;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionCheck {

  @Before("@annotation(checkPermission)")
  public void checkPermission(final CheckPermission checkPermission) {
    final String[] requiredPermissions = checkPermission.value();

    try {
      final AuthToken authToken = getAuthentication();
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

  public void checkProfileAccess(final String email, final long id) {
    try {
      final AuthToken authToken = getAuthentication();
      final boolean isSuperUser = checkSuperUser(authToken);
      final boolean isPermitted = checkUserIdEmail(email, id, authToken);

      if (!isSuperUser && !isPermitted) {
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
      final AuthToken authToken = getAuthentication();
      final boolean isSuperUser = checkSuperUser(authToken);

      if (isSuperUser) {
        return profileEntities;
      }

      return profileEntities.stream()
          .filter(profile -> checkUserIdEmail(profile.getEmail(), profile.getId(), authToken))
          .toList();
    } catch (Exception ex) {
      throw new CheckPermissionException(ex.getMessage());
    }
  }

  private AuthToken getAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || authentication.getPrincipal() == null
        || !authentication.isAuthenticated()) {
      throw new CheckPermissionException("Profile not authenticated...");
    }

    if (authentication.getCredentials() != null
        && authentication.getCredentials() instanceof AuthToken authToken) {
      return authToken;
    }

    throw new CheckPermissionException("Profile not authorized...");
  }

  private boolean checkUserPermission(
      final AuthToken authToken, final List<String> requiredPermissions) {
    final boolean isSuperUser = checkSuperUser(authToken);

    if (isSuperUser) {
      return true;
    }

    return authToken.getPermissions().stream()
        .anyMatch(
            authTokenPermission ->
                requiredPermissions.contains(authTokenPermission.getPermissionName()));
  }

  private boolean checkSuperUser(final AuthToken authToken) {
    return authToken.getRoles().stream()
        .anyMatch(authTokenRole -> authTokenRole.getRoleName().equals(ROLE_NAME_SUPERUSER));
  }

  private boolean checkUserIdEmail(final String email, final long id, final AuthToken authToken) {
    return Objects.equals(email, authToken.getProfile().getEmail())
        || Objects.equals(id, authToken.getProfile().getId());
  }
}
