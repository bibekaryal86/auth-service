package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.dto.Roles;

public interface RolesRepository extends JpaRepository<Roles, Integer> {}
