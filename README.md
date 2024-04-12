# Czetsuya Tech | Commons/Unified Kafka Vertical Events

Commons Kafka Vertical Event library is an implementation of the vertical event pattern that can be
used to guarantee the sending and receiving of messages across multiple applications using Kafka.

## Usage

1. Enable commons-kafka-vertical-events by using the annotation `@EnableUnifiedKafkaVerticalEvents`.

2. To implement a producer extend the class `AbstractKafkaProducer`.

```
package com.czetsuyatech.vertical.events.client.messaging.producers;

import com.czetsuyatech.vertical.events.client.messaging.messages.IamEvent;
import com.czetsuyatech.vertical.events.config.KafkaConfig;
import com.czetsuyatech.vertical.events.messaging.messages.KeyAttributes;
import com.czetsuyatech.vertical.events.messaging.messages.VerticalEventDTO;
import com.czetsuyatech.vertical.events.messaging.messages.VerticalEventDTO.Entity;
import com.czetsuyatech.vertical.events.messaging.messages.VerticalEventDTO.Event;
import com.czetsuyatech.vertical.events.messaging.producers.AbstractKafkaProducer;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@AllArgsConstructor
@Slf4j
public class IamEventProducer extends AbstractKafkaProducer<IamEvent, VerticalEventDTO> {

  private final KafkaConfig kafkaConfig;
  private final KafkaTemplate<String, VerticalEventDTO> kafkaTemplate;

  @Override
  protected CompletableFuture<SendResult<String, VerticalEventDTO>> publishMessage(String topic, VerticalEventDTO event) {
    return kafkaTemplate.send(topic, event.getEvent().getEventId(), event);
  }

  @Override
  protected void publishFailure(VerticalEventDTO message, Throwable throwable) {
    log.error("Message: {} failed to be sent to kafka", message, throwable);
  }

  @Override
  protected void publishSuccess(VerticalEventDTO message, SendResult<String, VerticalEventDTO> result) {

    log.debug(
        "Message: {} was sent to kafka with offset: {}",
        message,
        result.getRecordMetadata().offset());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Override
  public void publish(IamEvent event) {

    log.debug("Sending to topic={}, event={}", kafkaConfig.getTopics().getIamVertical(), event);

    sendMessage(
        kafkaConfig.getTopics().getIamVertical(),
        getVerticalEvent(event));
  }

  private VerticalEventDTO getVerticalEvent(IamEvent event) {

    return VerticalEventDTO.builder()
        .event(Event.builder()
            .timestamp(OffsetDateTime.now().toString())
            .service(event.getServiceName())
            .eventType(event.getEventType().name())
            .build())
        .entity(Entity.builder()
            .type(event.getEntityType())
            .keyAttributes(Map.of(
                KeyAttributes.EID, event.getEid()))
            .build())
        .build();
  }
}
```

3. To implement a consumer we need to define a bean config that extends `AbstractKafkaBeansConfig`.

```
package com.czetsuyatech.vertical.events.client.messaging.config;

import com.czetsuyatech.vertical.events.client.messaging.messages.EventType;
import com.czetsuyatech.vertical.events.config.AbstractKafkaBeansConfig;
import com.czetsuyatech.vertical.events.messaging.messages.VerticalEventDTO;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

@Slf4j
@AllArgsConstructor
@Configuration
@EnableKafka
@ConditionalOnProperty(
    value = AbstractKafkaBeansConfig.BACKOFF_POLICY_ENABLED,
    havingValue = "true",
    matchIfMissing = true
)
public class KafkaBeansConfig extends AbstractKafkaBeansConfig {

  @NotNull
  public RecordFilterStrategy<Object, Object> getRejectRecordFilterStrategy() {

    return consumerRecord -> {
      log.debug("RecordFilterStrategy: {} ", consumerRecord.value());
      VerticalEventDTO verticalEvent = (VerticalEventDTO) consumerRecord.value();

      return Optional.ofNullable(verticalEvent.getEvent())
          .filter(ev -> EventType.isPresent(ev.getEventType()))
          .map(
              ev -> {
                log.info("Consuming bulk manual with eventId={}", ev.getEventId());
                VerticalEventDTO.Entity entity = verticalEvent.getEntity();
                return Optional.ofNullable(entity).isEmpty();
              })
          .orElse(Boolean.TRUE);
    };
  }

  @Override
  protected Logger getLogger() {
    return log;
  }
}
```

4. Use the bean we created in step 3 in the concrete consumer.

```
package com.czetsuyatech.vertical.events.client.messaging.consumers;

import com.czetsuyatech.vertical.events.client.services.IamEventStrategy;
import com.czetsuyatech.vertical.events.messaging.messages.VerticalEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(value = "unified.kafka.vertical-events.enabled", havingValue = "true")
@Service
@Slf4j
@RequiredArgsConstructor
public class IamEventsConsumer {

  private static final String CONSUMER_TOPIC = "${unified.kafka.vertical-events.topics.iam-vertical}";
  private static final String CONSUMER_GROUP = "${unified.kafka.vertical-events.consumers.iam-group}";
  private final IamEventStrategy iamEventStrategy;

  @KafkaListener(
      topics = CONSUMER_TOPIC,
      groupId = CONSUMER_GROUP,
      containerFactory = "concurrentKafkaListenerContainerFactory")
  public void handleEvent(VerticalEventDTO verticalEventDTO) {

    log.debug("Receives event with type={}", verticalEventDTO.getEvent().getEventType());

    iamEventStrategy.processEvent(verticalEventDTO);
  }

  @DltHandler
  void handle(String in, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.debug("{} from {}", in, topic);
  }
}

```

## Repository

- https://github.com/czetsuyatech/commons-kafka-vertical-events-client
