package com.company.petplatform.common;

public final class LogMaskingUtil {
  private LogMaskingUtil() {
  }

  public static String mask(String value) {
    if (value == null || value.isBlank()) {
      return value;
    }
    if (value.length() <= 2) {
      return "**";
    }
    return value.substring(0, 1) + "***" + value.substring(value.length() - 1);
  }
}
