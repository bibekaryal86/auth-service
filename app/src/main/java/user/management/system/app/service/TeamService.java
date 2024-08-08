package user.management.system.app.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user.management.system.app.repository.TeamRepository;

@Slf4j
@Service
public class TeamService {

  private final TeamRepository teamRepository;

  public TeamService(TeamRepository teamRepository) {
    this.teamRepository = teamRepository;
  }

  public List<Team> getAllTeams(
      final int limit,
      final int offset,
      final boolean includeDeletedTeams,
      final boolean includeDeletedUsers,
      final boolean includeDeletedRoles) {
    log.debug(
        "Get All Teams: limit=[{}] offset=[{}] includeDeletedTeams=[{}] includeDeletedUsers=[{}] includeDeletedRoles=[{}]",
        limit,
        offset,
        includeDeletedTeams,
        includeDeletedUsers,
        includeDeletedRoles);
    return teamRepository.getAllTeams(
        limit, offset, includeDeletedTeams, includeDeletedUsers, includeDeletedRoles);
  }

  public Team getTeamById(
      final int id,
      final boolean includeDeletedTeams,
      final boolean includeDeletedUsers,
      final boolean includeDeletedRoles) {
    log.debug(
        "Get Team by Id: id=[{}] includeDeletedTeams=[{}] includeDeletedUsers[{}] includeDeletedRoles=[{}]",
        id,
        includeDeletedTeams,
        includeDeletedUsers,
        includeDeletedRoles);
    return teamRepository.getTeamById(
        id, includeDeletedTeams, includeDeletedUsers, includeDeletedRoles);
  }

  public int createTeam(final TeamRequest team) {
    log.debug("Create Team: [{}]", team);
    int newTeamId = teamRepository.createTeam(team);
    log.debug("Created Team: newTeamId=[{}]", newTeamId);
    return newTeamId;
  }

  public int updateTeam(final int teamId, final TeamRequest team) {
    log.debug("Update Team: [{}]", team);
    int updatedRows = teamRepository.updateTeam(teamId, team);
    log.debug("Updated Team: updatedRows=[{}]", updatedRows);
    return updatedRows;
  }

  public int deleteTeam(final int teamId, final boolean isHardDelete) {
    log.debug("Delete Team: id=[{}] isHardDelete=[{}]", teamId, isHardDelete);
    int deletedRows = teamRepository.deleteTeam(teamId, isHardDelete);
    log.debug("Deleted Team: deletedRows=[{}]", deletedRows);
    return deletedRows;
  }

  public int restoreTeam(final int teamId) {
    log.debug("Restore Team: id=[{}]", teamId);
    int restoredRows = teamRepository.restoreTeam(teamId);
    log.debug("Restored Team: restoredRows=[{}]", restoredRows);
    return restoredRows;
  }
}
