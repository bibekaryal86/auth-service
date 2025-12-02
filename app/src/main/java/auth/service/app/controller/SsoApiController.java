package auth.service.app.controller;

import auth.service.app.model.dto.ProfileResponse;
import auth.service.app.model.entity.PlatformProfileRoleEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.service.PlatformProfileRoleService;
import auth.service.app.util.CommonUtils;
import auth.service.app.util.EntityDtoConvertUtils;
import auth.service.app.util.PermissionCheck;
import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sso/{platformId}")
public class SsoApiController {

  private final PlatformProfileRoleService platformProfileRoleService;
  private final EntityDtoConvertUtils entityDtoConvertUtils;
  private final PermissionCheck permissionCheck;

  @GetMapping(value = "/validate/token", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AuthToken> validateToken(@PathVariable final Long platformId) {
    // Token is validated by Spring Security already, now need to return AuthToken
    try {
      final AuthToken authToken = CommonUtils.getAuthentication();
      if (authToken != null
          && authToken.getPlatform() != null
          && Objects.equals(platformId, authToken.getPlatform().getId())) {
        return ResponseEntity.ok(authToken);
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception ex) {
      log.error("Validate Token Error", ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping(value = "/check/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Boolean>> checkPermissions(
      @PathVariable final Long platformId, @RequestBody List<String> permissionsToCheck) {
    try {
      final AuthToken authToken = CommonUtils.getAuthentication();
      if (authToken != null
          && authToken.getPlatform() != null
          && Objects.equals(platformId, authToken.getPlatform().getId())) {
        return ResponseEntity.ok(permissionCheck.checkPermissionDuplicate(permissionsToCheck));
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception ex) {
      log.error("Check Permissions Error", ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping(value = "/ba_profiles/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProfileResponse> listSsoProfiles(@PathVariable final Long platformId) {
    try {
      final List<PlatformProfileRoleEntity> pprEntities =
          platformProfileRoleService.readPlatformProfileRolesByPlatformIds(
              List.of(platformId), Boolean.FALSE);
      final List<ProfileEntity> profileEntities =
          pprEntities.stream().map(PlatformProfileRoleEntity::getProfile).toList();
      return entityDtoConvertUtils.getResponseMultipleProfiles(profileEntities, Boolean.FALSE);
    } catch (Exception ex) {
      log.error("List Sso Profiles: [{}]", platformId, ex);
      return entityDtoConvertUtils.getResponseErrorProfile(ex);
    }
  }
}
