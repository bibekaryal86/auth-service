package auth.service.app.exception.handler;

import auth.service.app.exception.CheckPermissionException;
import auth.service.app.model.dto.ResponseMetadata;
import auth.service.app.model.dto.ResponseStatusInfo;
import auth.service.app.util.CommonUtils;
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
  public ResponseEntity<ResponseMetadata> handleCheckPermissionException(
      CheckPermissionException ex) {
    return new ResponseEntity<>(
        ResponseMetadata.builder()
            .responseStatusInfo(ResponseStatusInfo.builder().errMsg(ex.getMessage()).build())
            .responsePageInfo(CommonUtils.emptyResponsePageInfo())
            .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
            .build(),
        HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseMetadata> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    final String errMsg =
        ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
    return new ResponseEntity<>(
        ResponseMetadata.builder()
            .responseStatusInfo(ResponseStatusInfo.builder().errMsg(errMsg).build())
            .responsePageInfo(CommonUtils.emptyResponsePageInfo())
            .responseCrudInfo(CommonUtils.emptyResponseCrudInfo())
            .build(),
        HttpStatus.BAD_REQUEST);
  }
}
