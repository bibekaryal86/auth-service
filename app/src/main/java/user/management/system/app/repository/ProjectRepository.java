package user.management.system.app.repository;

import org.springframework.data.repository.CrudRepository;
import user.management.system.app.model.entity.ProjectEntity;

public interface ProjectRepository extends CrudRepository<ProjectEntity, Integer> {}
