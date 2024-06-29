package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.RoleEntity;

public interface RoleRepository extends CrudRepository<RoleEntity, Integer> {}
