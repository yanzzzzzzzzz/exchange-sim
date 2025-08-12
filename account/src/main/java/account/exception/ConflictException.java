package account.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
  private final String error;
  private final String message;

  public ConflictException(String error, String message) {
    super(message);
    this.error = error;
    this.message = message;
  }
}
