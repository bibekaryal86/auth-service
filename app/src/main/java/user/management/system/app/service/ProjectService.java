package user.management.system.app.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.management.system.app.repository.ProjectRepository;

@Slf4j
@Service
public class ProjectService {

  private final ProjectRepository projectRepository;

  public ProjectService(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  public List<Project> getAllProjects(
      final int limit,
      final int offset,
      final boolean includeDeletedProjects,
      final boolean includeDeletedUsers,
      final boolean includeDeletedRoles) {
    log.debug(
        "Get All Projects: limit=[{}] offset=[{}] includeDeletedProjects=[{}] includeDeletedUsers=[{}] includeDeletedRoles=[{}]",
        limit,
        offset,
        includeDeletedProjects,
        includeDeletedUsers,
        includeDeletedRoles);
    return projectRepository.getAllProjects(
        limit, offset, includeDeletedProjects, includeDeletedUsers, includeDeletedRoles);
  }

  public Project getProjectById(
      final int id,
      final boolean includeDeletedProjects,
      final boolean includeDeletedUsers,
      final boolean includeDeletedRoles) {
    log.debug(
        "Get Project by Id: id=[{}] includeDeletedProjects=[{}] includeDeletedUsers[{}] includeDeletedRoles=[{}]",
        id,
        includeDeletedProjects,
        includeDeletedUsers,
        includeDeletedRoles);
    return projectRepository.getProjectById(
        id, includeDeletedProjects, includeDeletedUsers, includeDeletedRoles);
  }

  public int createProject(final ProjectRequest project) {
    log.debug("Create Project: [{}]", project);
    int newProjectId = projectRepository.createProject(project);
    log.debug("Created Project: newProjectId=[{}]", newProjectId);
    return newProjectId;
  }

  public int updateProject(final int projectId, final ProjectRequest project) {
    log.debug("Update Project: [{}]", project);
    int updatedRows = projectRepository.updateProject(projectId, project);
    log.debug("Updated Project: updatedRows=[{}]", updatedRows);
    return updatedRows;
  }

  public int deleteProject(final int projectId, final boolean isHardDelete) {
    log.debug("Delete Project: id=[{}] isHardDelete=[{}]", projectId, isHardDelete);
    int deletedRows = projectRepository.deleteProject(projectId, isHardDelete);
    log.debug("Deleted Project: deletedRows=[{}]", deletedRows);
    return deletedRows;
  }

  public int restoreProject(final int projectId) {
    log.debug("Restore Project: id=[{}]", projectId);
    int restoredRows = projectRepository.restoreProject(projectId);
    log.debug("Restored Project: restoredRows=[{}]", restoredRows);
    return restoredRows;
  }
}
