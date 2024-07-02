package user.management.system.app.controller;

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
import user.management.system.app.model.dto.Role;
import user.management.system.app.model.dto.RoleRequest;
import user.management.system.app.model.dto.RoleResponse;
import user.management.system.app.service.RoleService;

@RestController
@RequestMapping("/roles")
public class RoleController {

  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @GetMapping
  public ResponseEntity<RoleResponse> getAllRoles(
      @RequestParam(required = false, defaultValue = "25") Integer limit,
      @RequestParam(required = false, defaultValue = "0") Integer offset,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedRoles,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedUsers) {
    try {
      List<Role> roles =
          roleService.getAllRoles(limit, offset, includeDeletedRoles, includeDeletedUsers);

      if (roles.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return ResponseEntity.ok(
          RoleResponse.builder()
              .roles(
                  roleService.getAllRoles(limit, offset, includeDeletedRoles, includeDeletedUsers))
              .build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          RoleResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<RoleResponse> getRoleById(
      @PathVariable("id") Integer id,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedRoles,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeletedUsers) {
    try {
      Role role = roleService.getRoleById(id, includeDeletedRoles, includeDeletedUsers);
      if (role == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return ResponseEntity.ok(
          RoleResponse.builder().roles(Collections.singletonList(role)).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          RoleResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping
  public ResponseEntity<RoleResponse> createRole(@RequestBody RoleRequest role) {
    try {
      int newId = roleService.createRole(role);
      if (newId <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return new ResponseEntity<>(
          RoleResponse.builder().createdRowsId(newId).build(), HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(
          RoleResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping(value = "/{id}")
  public ResponseEntity<RoleResponse> updateRole(
      @PathVariable("id") Integer id, @RequestBody RoleRequest role) {
    try {
      Role currentRole = roleService.getRoleById(id, true, false);
      if (currentRole == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int updatedRowsCount = roleService.updateRole(id, role);
      if (updatedRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(RoleResponse.builder().updatedRowsCount(updatedRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          RoleResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity<RoleResponse> deleteRole(
      @PathVariable("id") Integer id,
      @RequestParam(required = false, defaultValue = "false") Boolean isHardDelete) {
    try {
      Role currentRole = roleService.getRoleById(id, true, false);
      if (currentRole == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int deletedRowsCount = roleService.deleteRole(id, isHardDelete);
      if (deletedRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(RoleResponse.builder().deletedRowsCount(deletedRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          RoleResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PatchMapping(value = "/{id}")
  public ResponseEntity<RoleResponse> restoreRole(@PathVariable("id") Integer id) {
    try {
      Role currentRole = roleService.getRoleById(id, true, false);
      if (currentRole == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      int restoredRowsCount = roleService.restoreRole(id);
      if (restoredRowsCount <= 0) {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
      }
      return ResponseEntity.ok(RoleResponse.builder().deletedRowsCount(restoredRowsCount).build());
    } catch (Exception e) {
      return new ResponseEntity<>(
          RoleResponse.builder().error(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
