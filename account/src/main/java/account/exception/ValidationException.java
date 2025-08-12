package account.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {

  private final String error;
  private final String message;
  private final Map<String, List<String>> details;

  public ValidationException(String error, String message, Map<String, List<String>> details) {
    super(message);
    this.error = error;
    this.message = message;
    this.details = details;
  }
}
