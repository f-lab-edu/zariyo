package com.zariyo.service.event;

import org.springframework.context.ApplicationEvent;

public class QueueStartedEvent extends ApplicationEvent {
    public QueueStartedEvent(Object source) {
        super(source);
    }
}
