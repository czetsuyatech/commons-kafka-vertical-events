package com.czetsuyatech.vertical.events.config;

import com.czetsuyatech.vertical.events.ConditionalUniKafkaVerticalEventsEnabled;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalUniKafkaVerticalEventsEnabled
@ComponentScan(basePackages = {"com.czetsuyatech.vertical.events"})
@Import({KafkaConfig.class})
@EnableScheduling
@EnableAsync
public class UniKafkaVerticalEventsBootstrap {

}
