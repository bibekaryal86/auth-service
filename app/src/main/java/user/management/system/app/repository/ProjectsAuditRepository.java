package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.entities.ProjectsAuditEntity;

public interface ProjectsAuditRepository extends JpaRepository<ProjectsAuditEntity, Integer> {}
