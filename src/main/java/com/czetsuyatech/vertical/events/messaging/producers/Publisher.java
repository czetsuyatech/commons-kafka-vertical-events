package com.czetsuyatech.vertical.events.messaging.producers;

public interface Publisher<T> {

  /**
   * Publishes a message to a given broker.
   *
   * @param message the POJO message to be published
   */
  void publish(T message);
}
