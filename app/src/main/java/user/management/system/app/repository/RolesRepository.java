package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.dto.RolesDto;

public interface RolesRepository extends JpaRepository<RolesDto, Integer> {}
