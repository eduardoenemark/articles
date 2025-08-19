package br.com.eduardoenemark.pjrw.app.server.routing;

import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static br.com.eduardoenemark.pjrw.app.server.config.BeansConfiguration.LOGGER;

@Aspect
@Component
public class TransactionRoutingAspect {

    @Before("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation)")
    public void readBindResources(JoinPoint joinPoint) {
        LOGGER.debug("@Before: routeToRead");
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        RoutingPlatformTransactionManager.bindResources(OperationType.READ);
    }

    @AfterReturning("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.ReadOperation)")
    public void readUnbindResources(JoinPoint joinPoint) {
        LOGGER.debug("@AfterReturning: clearRead");
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.unbindResources();
    }

    @Before("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.WriteOperation)")
    public void writeBindResources(JoinPoint joinPoint) {
        LOGGER.debug("Write @Before Aspect: binding resources.");
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingPlatformTransactionManager.bindResources(OperationType.WRITE);
    }

    @AfterReturning("@annotation(br.com.eduardoenemark.pjrw.app.server.operation.annotation.WriteOperation)")
    public void writeUnbindResources(JoinPoint joinPoint) {
        LOGGER.debug("@AfterReturning: clearWrite");
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.unbindResources();
    }
}
