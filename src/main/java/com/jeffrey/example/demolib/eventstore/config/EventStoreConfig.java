package com.jeffrey.example.demolib.eventstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Event store configuration
 * - should only contain general configuration object that is globally accessible
 *   by any components at the lower-level
 *
 * @author Jeffrey Garcia Wong
 */
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

}
