package user.management.system.app.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import user.management.system.app.exception.CheckPermissionException;
import user.management.system.app.model.dto.ResponseStatusInfo;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CheckPermissionException.class)
  public ResponseEntity<ResponseStatusInfo> handleCheckPermissionException(
      CheckPermissionException ex) {
    return new ResponseEntity<>(
        ResponseStatusInfo.builder().errMsg(ex.getMessage()).build(), HttpStatus.FORBIDDEN);
  }
}
