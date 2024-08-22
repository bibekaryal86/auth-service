package user.management.system.app.util;

import static user.management.system.app.util.ConstantUtils.APP_ROLE_NAME_SUPERUSER;

import java.util.List;
import java.util.Objects;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import user.management.system.app.exception.CheckPermissionException;
import user.management.system.app.model.annotation.CheckPermission;
import user.management.system.app.model.token.AuthToken;

@Aspect
@Component
public class PermissionCheck {

  @Before("@annotation(checkPermission)")
  public void checkPermission(final CheckPermission checkPermission) {
    final String[] requiredPermissions = checkPermission.value();
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || authentication.getPrincipal() == null
        || !authentication.isAuthenticated()) {
      throw new CheckPermissionException("User not authenticated...");
    }

    try {
      final AuthToken authToken = (AuthToken) authentication.getCredentials();
      final boolean isPermitted = checkUserPermission(authToken, List.of(requiredPermissions));

      if (!isPermitted) {
        throw new CheckPermissionException("User does not have required permissions...");
      }
    } catch (Exception ex) {
      throw new CheckPermissionException(ex.getMessage());
    }
  }

  private boolean checkUserPermission(
      final AuthToken authToken, final List<String> requiredPermissions) {
    boolean isSuperUser =
        authToken.getRoles().stream()
            .anyMatch(authTokenRole -> authTokenRole.getName().equals(APP_ROLE_NAME_SUPERUSER));

    if (isSuperUser) {
      return true;
    }

    return authToken.getPermissions().stream()
        .anyMatch(
            authTokenPermission -> requiredPermissions.contains(authTokenPermission.getName()));
  }

  public void canUserAccessAppUser(final String email, final int id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || authentication.getPrincipal() == null
        || !authentication.isAuthenticated()) {
      throw new CheckPermissionException("User not authenticated...");
    }

    try {
      AuthToken authToken = (AuthToken) authentication.getCredentials();

      boolean isPermitted =
          Objects.equals(email, authToken.getUser().getEmail())
              || Objects.equals(id, authToken.getUser().getId());

      if (!isPermitted) {
        throw new CheckPermissionException(
            "User does not have required permissions to user entity...");
      }
    } catch (Exception ex) {
      throw new CheckPermissionException(ex.getMessage());
    }
  }
}
