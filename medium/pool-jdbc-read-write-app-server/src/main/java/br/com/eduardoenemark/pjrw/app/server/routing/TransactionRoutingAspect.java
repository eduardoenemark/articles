package br.com.eduardoenemark.pjrw.app.server.routing;

import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static br.com.eduardoenemark.pjrw.app.server.config.AppConfiguration.LOGGER;

@Aspect
@Component
public class TransactionRoutingAspect {

    @Before("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation)")
    public void readBindResources(JoinPoint joinPoint) {
        LOGGER.debug("Aspect @Before: read operation binding resources.");
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        RoutingPlatformTransactionManager.bindResources(OperationType.READ);
    }

    @AfterReturning("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation)")
    public void readUnbindResources(JoinPoint joinPoint) {
        LOGGER.debug("Aspect @AfterReturning: read operation unbinding resources");
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.unbindResources();
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
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.unbindResources();
    }
}
