package com.jeffrey.example.demolib.shutdown.filter;

import com.jeffrey.example.demolib.shutdown.service.GracefulShutdownService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component("gracefulShutdownProcessingFilter")
@Order(Ordered.HIGHEST_PRECEDENCE+1)
public class GracefulShutdownProcessingFilter implements WebFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdownProcessingFilter.class);

    private GracefulShutdownService gracefulShutdownService;

    public GracefulShutdownProcessingFilter(GracefulShutdownService gracefulShutdownService) {
        this.gracefulShutdownService = gracefulShutdownService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpResponse response = serverWebExchange.getResponse();
        if (gracefulShutdownService.isInvoked()) {
            LOGGER.debug("service shutdown in progress, declining request");
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return response.writeWith(
                    Mono.just(
                            new DefaultDataBufferFactory()
                                    .wrap("service shutdown in progress".getBytes())
                    )
            );
        }
        return webFilterChain.filter(serverWebExchange);
    }

}
