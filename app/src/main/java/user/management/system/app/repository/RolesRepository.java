package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.entity.RoleEntity;

public interface RolesRepository extends JpaRepository<RoleEntity, Integer> {}
