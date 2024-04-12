package com.czetsuyatech.vertical.events;

import com.czetsuyatech.vertical.events.config.Constants;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    prefix = Constants.PROPERTIES_PREFIX,
    name = {Constants.PROPERTY_ENABLED})
public @interface ConditionalUniKafkaVerticalEventsEnabled {

}
