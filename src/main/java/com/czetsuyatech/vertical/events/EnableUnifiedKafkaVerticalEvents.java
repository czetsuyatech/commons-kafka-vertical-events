package com.czetsuyatech.vertical.events;

import com.czetsuyatech.vertical.events.config.KafkaConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalUniKafkaVerticalEventsEnabled
@Import({KafkaConfig.class})
public @interface EnableUnifiedKafkaVerticalEvents {

}
