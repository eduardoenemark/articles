package br.com.eduardoenemark.pjrw.app.server.event;

import org.springframework.context.ApplicationEvent;

public class OperationContextEvent extends ApplicationEvent {

    public OperationContextEvent(Object source) {
        super(source);
    }

}
