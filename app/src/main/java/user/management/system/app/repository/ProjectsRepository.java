package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.entities.ProjectEntity;

public interface ProjectsRepository extends JpaRepository<ProjectEntity, Integer> {}
