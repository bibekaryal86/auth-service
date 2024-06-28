package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.entities.TeamEntity;

public interface TeamsRepository extends JpaRepository<TeamEntity, Integer> {}
