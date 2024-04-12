package com.czetsuyatech.vertical.events.messaging.messages;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerticalEventDTO {

  @Builder.Default
  @JsonAlias("schemaVersion")
  private String schemaVersion = "1.0";

  @JsonAlias("event")
  private Event event;

  @JsonAlias("entity")
  private Entity entity;

  @JsonAlias("metadata")
  private Map<String, String> metadata;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Event {

    @JsonAlias("eventId")
    private String eventId;

    @JsonAlias("timestamp")
    private String timestamp;

    @JsonAlias("environment")
    private String environment;

    @JsonAlias("service")
    private String service;

    @JsonAlias("eventType")
    private String eventType;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Entity {

    @JsonAlias("type")
    private String type;

    @JsonAlias("keyAttributes")
    private Map<String, String> keyAttributes;
  }
}
