package user.management.system.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.management.system.app.model.dto.ProjectsDto;

public interface ProjectsRepository extends JpaRepository<ProjectsDto, Integer> {}