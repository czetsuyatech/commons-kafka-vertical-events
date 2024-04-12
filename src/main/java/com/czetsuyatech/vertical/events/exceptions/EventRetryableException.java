package com.czetsuyatech.vertical.events.exceptions;

import lombok.Getter;

@Getter
public class EventRetryableException extends EventException {

  public EventRetryableException(String message) {

    super(message);
  }

  public EventRetryableException(String message, Throwable cause) {

    super(message, cause);
  }
}
