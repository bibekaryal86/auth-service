package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.dto.ProjectsAuditDto;

public interface ProjectsAuditRepository extends JpaRepository<ProjectsAuditDto, Integer> {}
