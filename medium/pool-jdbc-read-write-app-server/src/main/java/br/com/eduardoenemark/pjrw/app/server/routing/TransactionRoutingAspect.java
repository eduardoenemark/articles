package br.com.eduardoenemark.pjrw.app.server.routing;

import br.com.eduardoenemark.pjrw.app.server.event.OperationContextEvent;
import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static br.com.eduardoenemark.pjrw.app.server.config.AppConfiguration.LOGGER;

@Aspect
@Component
public class TransactionRoutingAspect {

    private final ApplicationEventPublisher publisher;

    @Autowired
    public TransactionRoutingAspect(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Async
    public void publishEvent(OperationContext context) {
        this.publisher.publishEvent(new OperationContextEvent(context));
    }

    @Before("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation)")
    public void readBindResources(JoinPoint joinPoint) {
        LOGGER.debug("Aspect @Before: read operation binding resources");
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        RoutingPlatformTransactionManager.bindResources(OperationType.READ);
    }

    @AfterReturning("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation)")
    public void readUnbindResources(JoinPoint joinPoint) {
        LOGGER.debug("Aspect @AfterReturning: read operation unbinding resources");
        RoutingPlatformTransactionManager.unbindResources();

        val context = RoutingPlatformTransactionManager.getThreadLocalContext().get();
        LOGGER.debug("Read operation duration: {}ms", context.diffMillis());

        // Publish event.
        publishEvent(context);

        // Reset context after event is published
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.reset();
    }

    @Before("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.WriteOperation)")
    public void writeBindResources(JoinPoint joinPoint) {
        LOGGER.debug("Aspect @Before: write operation binding resources");
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingPlatformTransactionManager.bindResources(OperationType.WRITE);
    }

    @AfterReturning("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.WriteOperation)")
    public void writeUnbindResources(JoinPoint joinPoint) {
        LOGGER.debug("Aspect @AfterReturning: write operation unbinding resources");
        RoutingPlatformTransactionManager.unbindResources();

        val context = RoutingPlatformTransactionManager.getThreadLocalContext().get();
        LOGGER.debug("Write operation duration: {}ms", context.diffMillis());

        // Publish event
        publishEvent(context);

        // Reset context after event is published
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.reset();
    }
}
