package auth.service.app.model.token;

public record AuthTokenRolePermissionLookup(
    Long roleId, String roleName, Long permissionId, String permissionName) {}
