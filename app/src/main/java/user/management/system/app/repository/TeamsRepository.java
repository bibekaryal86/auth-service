package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.dto.Teams;

public interface TeamsRepository extends JpaRepository<Teams, Integer> {}
