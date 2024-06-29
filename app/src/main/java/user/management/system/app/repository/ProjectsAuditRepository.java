package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.ProjectsAuditEntity;

public interface ProjectsAuditRepository extends CrudRepository<ProjectsAuditEntity, Integer> {}
