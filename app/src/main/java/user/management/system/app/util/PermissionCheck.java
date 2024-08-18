package user.management.system.app.util;

import static user.management.system.app.util.ConstantUtils.APP_ROLE_NAME_SUPERUSER;

import java.util.List;
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
    String[] requiredPermissions = checkPermission.value();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || authentication.getPrincipal() == null
        || !authentication.isAuthenticated()) {
      throw new CheckPermissionException("User not authenticated...");
    }

    try {
      AuthToken authToken = (AuthToken) authentication.getPrincipal();
      boolean isPermitted = checkUserPermission(authToken, List.of(requiredPermissions));

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
    boolean hasPermission =
        authToken.getPermissions().stream()
            .anyMatch(
                authTokenPermission -> requiredPermissions.contains(authTokenPermission.getName()));
    return isSuperUser || hasPermission;
  }

  // TODO user should be able to update their own user entities
  // check = should be logged in, app and email from token must match app and email, and ID should also match
}
