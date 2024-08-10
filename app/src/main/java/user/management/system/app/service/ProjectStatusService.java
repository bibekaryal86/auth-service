package user.management.system.app.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import user.management.system.app.exception.ElementNotFoundException;
import user.management.system.app.model.dto.ProjectStatusRequest;
import user.management.system.app.model.entity.ProjectStatusEntity;
import user.management.system.app.repository.ProjectStatusRepository;

@Service
@Slf4j
public class ProjectStatusService {
  private final ProjectStatusRepository projectStatusRepository;

  public ProjectStatusService(final ProjectStatusRepository projectStatusRepository) {
    this.projectStatusRepository = projectStatusRepository;
  }

  @CacheEvict(value = "projectStatuses", allEntries = true, beforeInvocation = true)
  public ProjectStatusEntity createProjectStatus(final ProjectStatusRequest projectStatusRequest) {
    log.debug("Create Project Status: [{}]", projectStatusRequest);
    ProjectStatusEntity projectStatusEntity = new ProjectStatusEntity();
    BeanUtils.copyProperties(projectStatusRequest, projectStatusEntity);
    return projectStatusRepository.save(projectStatusEntity);
  }

  @Cacheable("projectStatuses")
  public List<ProjectStatusEntity> retrieveProjectStatuses() {
    log.debug("Retrieve Project Statuses...");
    return projectStatusRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
  }

  public ProjectStatusEntity retrieveProjectStatusById(final int id) {
    log.debug("Retrieve Project Status By Id: [{}]", id);
    return projectStatusRepository
        .findById(id)
        .orElseThrow(() -> new ElementNotFoundException("Project Status", String.valueOf(id)));
  }

  @CacheEvict(value = "projectStatuses", allEntries = true, beforeInvocation = true)
  public ProjectStatusEntity updateProjectStatus(
      final int id, final ProjectStatusRequest projectStatusRequest) {
    log.debug("Update Project Status: [{}], [{}]", id, projectStatusRequest);
    ProjectStatusEntity projectStatusEntity = retrieveProjectStatusById(id);
    BeanUtils.copyProperties(projectStatusRequest, projectStatusEntity);
    return projectStatusRepository.save(projectStatusEntity);
  }

  @CacheEvict(value = "projectStatuses", allEntries = true, beforeInvocation = true)
  public void deleteProjectStatus(final int id) {
    log.info("Delete Project Status: [{}]", id);
    ProjectStatusEntity projectStatusEntity = retrieveProjectStatusById(id);
    projectStatusRepository.delete(projectStatusEntity);
  }
}
