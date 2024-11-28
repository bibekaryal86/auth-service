package auth.service.app.exception.handler;

import auth.service.app.exception.CheckPermissionException;
import auth.service.app.model.dto.ResponseStatusInfo;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    final String errMsg =
        ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
    return new ResponseEntity<>(
        ResponseStatusInfo.builder().errMsg(errMsg).build(), HttpStatus.BAD_REQUEST);
  }
}
