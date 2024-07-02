package user.management.system.app.controller;

import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user.management.system.app.model.dto.RoleResponse;
import user.management.system.app.service.RoleService;

@RestController
public class RoleController {

  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RoleResponse> getAllRoles(
      @RequestParam(required = false, defaultValue = "0") Integer limit,
      @RequestParam(required = false, defaultValue = "0") Integer offset,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedRoles,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedUsers) {
    try {
      return ResponseEntity.ok(
          RoleResponse.builder()
              .roles(
                  roleService.getAllRoles(limit, offset, includeDeletedRoles, includeDeletedUsers))
              .build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          RoleResponse.builder().roles(Collections.emptyList()).error(e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
