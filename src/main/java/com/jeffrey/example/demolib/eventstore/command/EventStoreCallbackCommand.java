package com.jeffrey.example.demolib.eventstore.command;

import com.jeffrey.example.demolib.eventstore.entity.DomainEvent;
import com.jeffrey.example.demolib.eventstore.dao.EventStoreDao;

/**
 * Represents a callback operation that should be invoked upon the trigger
 * {@link #pendingEventFetched(DomainEvent)}
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #pendingEventFetched(DomainEvent)}.
 *
 * @see EventStoreDao
 * @author Jeffrey Garcia Wong
 */
@FunctionalInterface
public interface EventStoreCallbackCommand {

    /**
     * Execute the callback operation against the {@link DomainEvent} and
     * return no result.
     *
     * @param domainEvent the domain event object to operate against
     */
    void pendingEventFetched(DomainEvent domainEvent) throws Exception;

}
