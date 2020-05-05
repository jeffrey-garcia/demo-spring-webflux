package com.jeffrey.example.demolib.eventstore.config;

import com.jeffrey.example.demolib.eventstore.aop.aspect.ReactiveEventStoreAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;

import static com.jeffrey.example.demolib.eventstore.util.ChannelBindingAccessor.GLOBAL_ERROR_CHANNEL;
import static com.jeffrey.example.demolib.eventstore.util.ChannelBindingAccessor.GLOBAL_PUBLISHER_CONFIRM_CHANNEL;

/**
 * There are 2 ways to register aspect classes:
 * 1) manually register aspect classes as regular beans, or
 * 2) autodetect them through classpath scanning 
 *
 * If you opt for autodetect through classpath scanning,
 * you are required to add a separate @Component annotation
 * to the aspect class together with @Aspect annotation
 */
//@ComponentScan("com.example.jeffrey.demolib.eventstore.aop.aspect")
/**
 * Configuration class which hook up event store components with externalized configuration
 *
 * @see ReactiveEventStoreAspect
 * @author Jeffrey Garcia Wong
 */
@EnableAspectJAutoProxy
@Configuration("EventStoreConfig")
public class EventStoreConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventStoreConfig.class);

    @Value("${com.jeffrey.example.eventstore.timezone:#{null}}") // zoneIdString default null if undefined
    String zoneIdString;

    /**
     * Create a global system {@link Clock} configured with a specified timezone
     * (Zone ID String).
     *
     * <p>
     * Default to system timezone if undefined (not recommended if the system is
     * distributed to run in multiple geographical locations which may observe
     * difference in timezone.
     * </p>
     *
     * @return a {@link Clock}
     */
    @Bean("eventStoreClock")
    public Clock eventStoreClock() {
        Clock clock = zoneIdString != null ? Clock.system(ZoneId.of(zoneIdString)):Clock.systemDefaultZone();
        return clock;
    }

    /**
     * Define an {@link IdGenerator} that uses {@link java.security.SecureRandom} for the initial
     * seed and Random thereafter, instead of calling {@link UUID#randomUUID()} every time
     * as {@link org.springframework.util.JdkIdGenerator} does.
     *
     * <p>This provides a better balance between securely random ids and performance.</p>
     *
     * @return an {@link AlternativeJdkIdGenerator}
     */
    @Bean("eventIdGenerator")
    public IdGenerator idGenerator() {
        return new AlternativeJdkIdGenerator();
    }

    /**
     * manually register aspect class as regular bean
     */
    @Bean
    public ReactiveEventStoreAspect demoAspect() {
        return new ReactiveEventStoreAspect();
    }

}
