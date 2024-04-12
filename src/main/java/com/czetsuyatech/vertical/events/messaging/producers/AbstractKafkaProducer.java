package com.czetsuyatech.vertical.events.messaging.producers;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;

@Slf4j
public abstract class AbstractKafkaProducer<EVENT, VERTICAL> implements Publisher<EVENT> {

  protected abstract CompletableFuture<SendResult<String, VERTICAL>> publishMessage(String topic, VERTICAL message);

  protected void sendMessage(String topic, VERTICAL message) {

    CompletableFuture<SendResult<String, VERTICAL>> future = publishMessage(topic, message);

    future.whenComplete((result, ex) -> {
      if (ex != null) {
        publishFailure(message, ex);

      } else {
        publishSuccess(message, result);
      }
    });
  }

  protected abstract void publishFailure(VERTICAL message, Throwable throwable);

  protected abstract void publishSuccess(VERTICAL message, SendResult<String, VERTICAL> result);
}
