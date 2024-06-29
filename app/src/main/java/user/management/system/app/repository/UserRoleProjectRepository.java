package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.UserRoleProjectEntity;

public interface UserRoleProjectRepository extends CrudRepository<UserRoleProjectEntity, Integer> {}
