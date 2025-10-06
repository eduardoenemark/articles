package br.com.eduardoenemark.pjrw.app.server.config.routing;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static br.com.eduardoenemark.pjrw.app.server.config.BeansConfiguration.LOGGER;

@Aspect
@Component
public class RoutingTransactionAspect {

    @Before("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation)")
    public void readBindResources() {
        LOGGER.debug("Aspect @Before: read operation binding resources");
        RoutingTransaction.readBindResources();
    }

    @AfterReturning("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation)")
    public void readUnbindResources() {
        LOGGER.debug("Aspect @AfterReturning: read operation unbinding resources");
        RoutingTransaction.readUnbindResources();
    }

    @Before("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.WriteOperation)")
    public void writeBindResources() {
        LOGGER.debug("Aspect @Before: write operation binding resources");
        RoutingTransaction.writeBindResources();
    }

    @AfterReturning("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.WriteOperation)")
    public void writeUnbindResources() {
        LOGGER.debug("Aspect @AfterReturning: write operation unbinding resources");
        RoutingTransaction.writeUnbindResources();
    }
}
