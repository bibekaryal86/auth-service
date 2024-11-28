package auth.service.app.exception.wrapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionDetails {
  private String exceptionType;
  private String message;
  private String stackTrace;
  private String cause;

  public ExceptionDetails(Exception exception) {
    this.exceptionType = exception.getClass().getName();
    this.message = exception.getMessage();
    this.stackTrace = getStackTraceAsString(exception);
    this.cause = exception.getCause() == null ? null : exception.getCause().toString();
  }

  private String getStackTraceAsString(Exception exception) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);
    return sw.toString();
  }
}
