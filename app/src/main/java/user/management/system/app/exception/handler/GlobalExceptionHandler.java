package user.management.system.app.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseStatusInfo> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    StringBuilder stringBuilder = new StringBuilder();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              stringBuilder.append(error.getDefaultMessage());
            });
    return new ResponseEntity<>(
        ResponseStatusInfo.builder().errMsg(stringBuilder.toString()).build(),
        HttpStatus.BAD_REQUEST);
  }
}
