package com.fintra.stocktrading.exception;

public class OrderMatchingException extends RuntimeException {
  public OrderMatchingException(String message) {
    super(message);
  }

  public OrderMatchingException(String message, Throwable cause) {
    super(message, cause);
  }
}
