package user.management.system.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "App Test Controller", description = "Some miscellaneous tests for application")
@RestController
public class AppTestController {
  @CrossOrigin
  @GetMapping(value = "/tests/ping", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Ping test",
      description = "Returns a successful ping response",
      tags = {"App Test Controller"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful ping response",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<String> pingTest() {
    return ResponseEntity.ok("{\"ping\": \"successful\"}");
  }
}
