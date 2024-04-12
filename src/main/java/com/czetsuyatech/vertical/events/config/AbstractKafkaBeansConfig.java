package com.czetsuyatech.vertical.events.config;

import com.czetsuyatech.vertical.events.exceptions.EventFailedException;
import com.czetsuyatech.vertical.events.exceptions.EventRetryableException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.util.backoff.ExponentialBackOff;

public abstract class AbstractKafkaBeansConfig {

  public static final String BACKOFF_POLICY_ENABLED = "unified.kafka.vertical-events.back-off-policy.enabled";

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> concurrentKafkaListenerContainerFactory(
      KafkaConfig kafkaConfig, ConsumerFactory<Object, Object> consumerFactory,
      KafkaTemplate<String, String> kafkaTemplate) {

    ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setAckDiscarded(true);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
    factory.setCommonErrorHandler(errorHandler(kafkaTemplate, kafkaConfig));
    factory.setRecordFilterStrategy(getRejectRecordFilterStrategy());
    return factory;
  }

  @Bean
  public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate, KafkaConfig kafkaConfig) {

    ExponentialBackOff exponentialBackOff = new ExponentialBackOff();
    exponentialBackOff.setInitialInterval(kafkaConfig.getBackoffPolicy().getInitialInterval());
    exponentialBackOff.setMaxInterval(kafkaConfig.getBackoffPolicy().getMaxInterval());
    exponentialBackOff.setMultiplier(kafkaConfig.getBackoffPolicy().getMultiplier());
    exponentialBackOff.setMaxAttempts(kafkaConfig.getBackoffPolicy().getMaxRetry());

    var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
        (consumerRecord, exception) -> {
          getLogger().error("Fail processing message={}, exception={}", consumerRecord.value(), exception.getMessage());
          return new TopicPartition(consumerRecord.topic() + "-dlt", consumerRecord.partition());
        });

    DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, exponentialBackOff);
    errorHandler.addRetryableExceptions(EventRetryableException.class);
    errorHandler.addNotRetryableExceptions(EventFailedException.class);

    return errorHandler;
  }

  protected abstract RecordFilterStrategy<Object, Object> getRejectRecordFilterStrategy();

  protected abstract Logger getLogger();
}
