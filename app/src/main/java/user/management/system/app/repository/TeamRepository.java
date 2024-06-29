package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.TeamEntity;

public interface TeamRepository extends CrudRepository<TeamEntity, Integer> {}
