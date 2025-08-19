package br.com.eduardoenemark.pjrw.app.server.routing;

import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static br.com.eduardoenemark.pjrw.app.server.config.BeansConfiguration.LOGGER;

public class RoutingPlatformTransactionManager implements PlatformTransactionManager {

    private static final Map<OperationType, PlatformTransactionManager> transactionManagers = new HashMap<>();
    private static final Map<OperationType, LocalContainerEntityManagerFactoryBean> localContainerEntityManagerFactories = new HashMap<>();
    private static final ThreadLocal<OperationType> CONTEXT = new ThreadLocal<>();
    private static RoutingDataSource routingDataSource;

    // Set context BEFORE transaction starts
    public static void bindResources(OperationType type) {
        LOGGER.debug("Binding resources for operation type {}", type);
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            LOGGER.debug("No active transaction found, binding resources for operation type {}", type);
            setOperationType(type);
            TransactionSynchronizationManager.bindResource(OperationType.class, type);
            TransactionSynchronizationManager.bindResource(DataSource.class, routingDataSource);
            TransactionSynchronizationManager.bindResource(RoutingDataSource.class, routingDataSource);
            TransactionSynchronizationManager.bindResource(RoutingPlatformTransactionManager.class, transactionManagers.get(type));
            TransactionSynchronizationManager.bindResource(LocalContainerEntityManagerFactoryBean.class, localContainerEntityManagerFactories.get(type));
        }
    }

    // Reset AFTER transaction completes
    public static void unbindResources() {
        LOGGER.debug("Unbinding resources. Actual transaction active: {}", TransactionSynchronizationManager.isActualTransactionActive());
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.unbindResource(OperationType.class);
            TransactionSynchronizationManager.unbindResource(DataSource.class);
            TransactionSynchronizationManager.unbindResource(RoutingDataSource.class);
            TransactionSynchronizationManager.unbindResource(RoutingPlatformTransactionManager.class);
            TransactionSynchronizationManager.unbindResource(LocalContainerEntityManagerFactoryBean.class);
        }
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        OperationType type = getCurrentOperationType();
        return transactionManagers.get(type).getTransaction(definition);
    }

    @Override
    public void commit(TransactionStatus status) {
        OperationType type = getCurrentOperationType();
        if (OperationType.WRITE.equals(type)) {
            transactionManagers.get(type).commit(status);
        }
    }

    @Override
    public void rollback(TransactionStatus status) {
        OperationType type = getCurrentOperationType();
        if (OperationType.WRITE.equals(type)) {
            transactionManagers.get(type).rollback(status);
        }
    }

    public static void setOperationType(OperationType type) {
        CONTEXT.set(type);
    }

    public static OperationType getCurrentOperationType() {
        return CONTEXT.get() != null ? CONTEXT.get() : OperationType.WRITE;
    }

    public RoutingPlatformTransactionManager add(OperationType type, PlatformTransactionManager transactionManager) {
        transactionManagers.put(type, transactionManager);
        return this;
    }

    public RoutingPlatformTransactionManager add(OperationType type, LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        localContainerEntityManagerFactories.put(type, entityManagerFactoryBean);
        return this;
    }

    public RoutingPlatformTransactionManager add(RoutingDataSource routingDataSource) {
        RoutingPlatformTransactionManager.routingDataSource = routingDataSource;
        return this;
    }
}
