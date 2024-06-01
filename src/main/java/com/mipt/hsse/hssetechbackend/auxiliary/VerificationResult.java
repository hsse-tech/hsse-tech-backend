package com.mipt.hsse.hssetechbackend.auxiliary;

import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VerificationResult {
  @Getter private final boolean isValid;
  private final String errorMessage;

  public static VerificationResult buildValid() {
    return new VerificationResult(true, "");
  }

  public static VerificationResult buildInvalid(@NotEmpty String errorMessage) {
    return new VerificationResult(false, errorMessage);
  }

  public void throwIfInvalid() {
    if (!isValid) {
      throw new VerificationFailedException(errorMessage);
    }
  }

  public String getErrorMessage() {
    if (isValid)
      throw new UnsupportedOperationException(
          "Cannot return error message because the verification was valid");
    return errorMessage;
  }
}
