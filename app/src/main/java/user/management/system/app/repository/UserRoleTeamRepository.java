package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.UserRoleTeamEntity;

public interface UserRoleTeamRepository extends CrudRepository<UserRoleTeamEntity, Integer> {}