package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.UserRoleEntity;

public interface UserRoleRepository extends CrudRepository<UserRoleEntity, Integer> {}
