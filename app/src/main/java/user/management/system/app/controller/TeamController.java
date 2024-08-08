package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.service.TeamService;

@RestController
@RequestMapping("/teams")
public class TeamController {

  private final TeamService teamService;

  public TeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  @Operation(summary = "Retrieve All Teams")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Teams found"),
        @ApiResponse(responseCode = "204", description = "Teams not found"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @GetMapping
  public ResponseEntity<TeamResponse> getAllTeams(
      @RequestParam(required = false, defaultValue = "25") Integer limit,
      @RequestParam(required = false, defaultValue = "0") Integer offset,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedTeams,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedUsers,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedRoles) {
    try {
      List<Team> teams =
          teamService.getAllTeams(
              limit, offset, includeDeletedTeams, includeDeletedUsers, includeDeletedRoles);

      if (teams.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return ResponseEntity.ok(TeamResponse.builder().teams(teams).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          TeamResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Retrieve One Team By ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Team found"),
        @ApiResponse(responseCode = "404", description = "Team not found by provided id"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @GetMapping(value = "/{id}")
  public ResponseEntity<TeamResponse> getTeamById(
      @PathVariable("id") Integer id,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedTeams,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedUsers,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedRoles) {
    try {
      Team team =
          teamService.getTeamById(
              id, includeDeletedTeams, includeDeletedUsers, includeDeletedRoles);
      if (team == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return ResponseEntity.ok(
          TeamResponse.builder().teams(Collections.singletonList(team)).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          TeamResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Create One Team")
  @PostMapping
  public ResponseEntity<TeamResponse> createTeam(@RequestBody TeamRequest team) {
    try {
      int newId = teamService.createTeam(team);
      if (newId <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return new ResponseEntity<>(
          TeamResponse.builder().createdRowsId(newId).build(), HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(
          TeamResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Update One Team")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Team successfully updated"),
        @ApiResponse(responseCode = "404", description = "Team not found by provided id"),
        @ApiResponse(responseCode = "503", description = "Team was not updated"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @PutMapping(value = "/{id}")
  public ResponseEntity<TeamResponse> updateTeam(
      @PathVariable("id") Integer id, @RequestBody TeamRequest team) {
    try {
      Team currentTeam = teamService.getTeamById(id, true, true, true);
      if (currentTeam == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int updatedRowsCount = teamService.updateTeam(id, team);
      if (updatedRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(TeamResponse.builder().updatedRowsCount(updatedRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          TeamResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Delete One Team, Requires SuperUser Permissions for Hard Delete")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Team successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Team not found by provided id"),
        @ApiResponse(responseCode = "503", description = "Team was not deleted"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @DeleteMapping(value = "/{id}")
  public ResponseEntity<TeamResponse> deleteTeam(
      @PathVariable("id") Integer id,
      @RequestParam(required = false, defaultValue = "false") Boolean isHardDelete) {
    try {
      Team currentTeam = teamService.getTeamById(id, true, true, true);
      if (currentTeam == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int deletedRowsCount = teamService.deleteTeam(id, isHardDelete);
      if (deletedRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(TeamResponse.builder().deletedRowsCount(deletedRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          TeamResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Restore One Soft Deleted Team, Requires SuperUser Permissions")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Team successfully restored"),
        @ApiResponse(responseCode = "404", description = "Team not found by provided id"),
        @ApiResponse(responseCode = "503", description = "Team was not restored"),
        @ApiResponse(responseCode = "500", description = "Something went wrong")
      })
  @PatchMapping(value = "/{id}")
  public ResponseEntity<TeamResponse> restoreTeam(@PathVariable("id") Integer id) {
    try {
      Team currentTeam = teamService.getTeamById(id, true, true, true);
      if (currentTeam == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int restoredRowsCount = teamService.restoreTeam(id);
      if (restoredRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(TeamResponse.builder().deletedRowsCount(restoredRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          TeamResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
