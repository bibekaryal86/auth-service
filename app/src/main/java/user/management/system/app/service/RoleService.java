package user.management.system.app.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.management.system.app.model.dto.Role;
import user.management.system.app.repository.RoleRepository;

@Slf4j
@Service
public class RoleService {

  private final RoleRepository roleRepository;

  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public List<Role> getAllRoles(
      int limit, int offset, boolean includeDeletedRoles, boolean includeDeletedUsers) {
    return roleRepository.getAllRoles(limit, offset, includeDeletedRoles, includeDeletedUsers);
  }
}
