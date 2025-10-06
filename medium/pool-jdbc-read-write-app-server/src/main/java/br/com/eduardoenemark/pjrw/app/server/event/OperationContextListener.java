package br.com.eduardoenemark.pjrw.app.server.event;

import br.com.eduardoenemark.pjrw.app.server.routing.OperationContext;
import lombok.val;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

import static br.com.eduardoenemark.pjrw.app.server.config.AppConfiguration.LOGGER;

@Component
public class OperationContextListener implements ApplicationListener<OperationContextEvent> {

    private static final LinkedList<OperationContextEvent> events = new LinkedList<>();

    @Async
    @Override
    public void onApplicationEvent(OperationContextEvent event) {
        val context = (OperationContext) event.getSource();
        LOGGER.debug("Operation log event: {}", context);
        events.add(event);
    }

    public static LinkedList<OperationContextEvent> getEvents() {
        return events;
    }
}
