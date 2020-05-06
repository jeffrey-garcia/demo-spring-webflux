package com.jeffrey.example.demolib.shutdown.config;

import com.jeffrey.example.demolib.shutdown.service.GracefulShutdownService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import sun.misc.Signal;
import sun.misc.SignalHandler;

@RefreshScope
@Configuration
public class GracefulShutdownConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdownConfig.class);

    // default to 5000ms if not defined
    @SuppressWarnings("unused")
    @Value("${com.jeffrey.example.gracefulShutdown.shutdownHook.timeoutMillis:5000}")
    private int shutdownHookTimeout;

    @SuppressWarnings({"unused"})
    @RefreshScope
    @Bean("sigIntHandler")
    public SignalHandler sigIntHandler(
            @Autowired ApplicationContext context,
            @Autowired GracefulShutdownService gracefulShutdownService)
    {
        LOGGER.debug("Registering signal interrupt handler...");
        return Signal.handle(new Signal("INT"), new SignalHandler() {
            private static final int SIGINT_EXIT_CODE = 130;

            @Override
            public void handle(Signal signal) {
                LOGGER.debug("Signal interrupt fired");
                gracefulShutdownService.invoke(SIGINT_EXIT_CODE, shutdownHookTimeout);
            }
        });
    }

    @SuppressWarnings({"unused"})
    @RefreshScope
    @Bean("sigTermHandler")
    public SignalHandler sigTermHandler(
            @Autowired ApplicationContext context,
            @Autowired GracefulShutdownService gracefulShutdownService)
    {
        LOGGER.debug("Registering signal interrupt handler...");
        return Signal.handle(new Signal("TERM"), new SignalHandler() {
            private static final int SIGTERM_EXIT_CODE = 143;

            @Override
            public void handle(Signal signal) {
                LOGGER.debug("Signal termination fired");
                gracefulShutdownService.invoke(SIGTERM_EXIT_CODE, shutdownHookTimeout);
            }
        });
    }

    @SuppressWarnings("unused")
    @Bean("gracefulShutdownContainerCloseListener")
    ApplicationListener<ContextClosedEvent> gracefulShutdownContainerCloseListener() {
        return contextClosedEvent -> LOGGER.debug("Spring container is closed");
    }

}
