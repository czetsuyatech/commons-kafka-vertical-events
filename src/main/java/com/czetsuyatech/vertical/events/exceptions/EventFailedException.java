package com.czetsuyatech.vertical.events.exceptions;

import lombok.Getter;

@Getter
public class EventFailedException extends EventException {

  public EventFailedException(String message) {

    super(message);
  }

  public EventFailedException(String message, Throwable cause) {

    super(message, cause);
  }
}
