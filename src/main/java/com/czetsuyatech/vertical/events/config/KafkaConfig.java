package com.czetsuyatech.vertical.events.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.validation.annotation.Validated;

@Data
@Slf4j
@Validated
@EnableKafka
@Configuration
@ConfigurationProperties(prefix = "unified.kafka.vertical-events")
public class KafkaConfig {

  @NotNull
  private Topics topics;

  @NotNull
  private BackoffPolicy backoffPolicy;

  private Producers producers;

  @Data
  @Validated
  public static class Topics {

    @NotEmpty
    private String iamVertical;
  }

  @Data
  @Validated
  public static class BackoffPolicy {

    @Min(30000)
    private Long initialInterval;

    @Min(600000)
    private Long maxInterval;

    @Min(1)
    private Long multiplier;

    @Min(3)
    private Integer maxRetry;
  }

  @Data
  @Validated
  public static class Producers {

    private boolean enabled;
  }
}
