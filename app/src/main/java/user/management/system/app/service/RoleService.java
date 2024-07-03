package user.management.system.app.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.management.system.app.model.dto.Role;
import user.management.system.app.model.dto.RoleRequest;
import user.management.system.app.repository.RoleRepository;

@Slf4j
@Service
public class RoleService {

  private final RoleRepository roleRepository;

  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public List<Role> getAllRoles(
      final int limit,
      final int offset,
      final boolean includeDeletedRoles,
      final boolean includeDeletedUsers) {
    log.debug(
        "Get All Roles: limit=[{}] offset=[{}] includeDeletedRoles=[{}] includeDeletedUsers=[{}]",
        limit,
        offset,
        includeDeletedRoles,
        includeDeletedUsers);
    return roleRepository.getAllRoles(limit, offset, includeDeletedRoles, includeDeletedUsers);
  }

  public Role getRoleById(
      final int id, final boolean includeDeletedRoles, final boolean includeDeletedUsers) {
    log.debug(
        "Get Role by Id: id=[{}] includeDeletedRoles=[{}] includeDeletedUsers[{}]",
        id,
        includeDeletedRoles,
        includeDeletedUsers);
    return roleRepository.getRoleById(id, includeDeletedRoles, includeDeletedUsers);
  }

  public int createRole(final RoleRequest role) {
    log.debug("Create Role: [{}]", role);
    int newRoleId = roleRepository.createRole(role.getName(), role.getDescription(), role.getStatus());
    log.debug("Created Role: newRoleId=[{}]", newRoleId);
    return newRoleId;
  }

  public int updateRole(final int roleId, final RoleRequest role) {
    log.debug("Update Role: [{}]", role);
    int updatedRows =
        roleRepository.updateRole(roleId, role.getName(), role.getDescription(), role.getStatus());
    log.debug("Updated Role: updatedRows=[{}]", updatedRows);
    return updatedRows;
  }

  public int deleteRole(final int roleId, final boolean isHardDelete) {
    log.debug("Delete Role: id=[{}] isHardDelete=[{}]", roleId, isHardDelete);
    int deletedRows = roleRepository.deleteRole(roleId, isHardDelete);
    log.debug("Deleted Role: deletedRows=[{}]", deletedRows);
    return deletedRows;
  }

  public int restoreRole(final int roleId) {
    log.debug("Restore Role: id=[{}]", roleId);
    int restoredRows = roleRepository.restoreRole(roleId);
    log.debug("Restored Role: restoredRows=[{}]", restoredRows);
    return restoredRows;
  }
}
