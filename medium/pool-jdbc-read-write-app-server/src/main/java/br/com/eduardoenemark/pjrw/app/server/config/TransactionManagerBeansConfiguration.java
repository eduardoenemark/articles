package br.com.eduardoenemark.pjrw.app.server.config;

import br.com.eduardoenemark.pjrw.app.server.config.routing.RoutingDataSource;
import br.com.eduardoenemark.pjrw.app.server.config.routing.RoutingPlatformTransactionManager;
import br.com.eduardoenemark.pjrw.app.server.config.routing.RoutingTransaction;
import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import jakarta.transaction.UserTransaction;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import java.util.function.Consumer;

import static br.com.eduardoenemark.pjrw.app.server.config.DataSourceBeansConfiguration.*;

@Configuration
public class TransactionManagerBeansConfiguration {

    public static final String READ_TRANSACTION_MANAGER = "readTransactionManager";
    public static final String WRITE_TRANSACTION_MANAGER = "writeTransactionManager";

    @Primary
    @Bean("transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier(ROUTING_DATASOURCE) RoutingDataSource routingDataSource,
                                                         @Qualifier(READ_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean readEntityManagerFactory,
                                                         @Qualifier(WRITE_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean writeEntityManagerFactory,
                                                         @Qualifier(READ_TRANSACTION_MANAGER) PlatformTransactionManager readTransactionManager,
                                                         @Qualifier(WRITE_TRANSACTION_MANAGER) PlatformTransactionManager writeTransactionManager) {
        return new RoutingPlatformTransactionManager()
                .add(routingDataSource)
                .add(OperationType.READ, readEntityManagerFactory)
                .add(OperationType.WRITE, writeEntityManagerFactory)
                .add(OperationType.READ, readTransactionManager)
                .add(OperationType.WRITE, writeTransactionManager);
    }

    @Bean(name = "writeTransactionTemplate")
    public TransactionTemplate writeTransactionTemplate(@Qualifier("transactionManager") PlatformTransactionManager platformTransactionManager) {
        val tt = new TransactionTemplate() {
            @Override
            public void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
                super.executeWithoutResult(status -> {
                    RoutingTransaction.writeBindResources();
                    action.accept(status);
                    status.flush();
                    RoutingTransaction.writeUnbindResources();
                });
            }

            @Override
            public <T> T execute(TransactionCallback<T> action) throws TransactionException {
                return super.execute(status -> {
                    try {
                        RoutingTransaction.writeBindResources();
                        return action.doInTransaction(status);
                    } finally {
                        status.flush();
                        RoutingTransaction.writeUnbindResources();
                    }
                });
            }
        };
        tt.setName("transactionTemplate");
        tt.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        tt.setTransactionManager(platformTransactionManager);
        tt.setIsolationLevel(TransactionTemplate.ISOLATION_DEFAULT);
        tt.setReadOnly(false);
        return tt;
    }

    @Bean(name = READ_TRANSACTION_MANAGER)
    public PlatformTransactionManager readTransactionManager(@Qualifier(ROUTING_DATASOURCE) DataSource dataSource,
                                                             @Qualifier(READ_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean emfb) {
        val tm = new JpaTransactionManager();
        tm.setDataSource(dataSource);
        tm.setEntityManagerFactory(emfb.getObject());
        return tm;
    }

    @Bean(name = WRITE_TRANSACTION_MANAGER)
    public PlatformTransactionManager writeTransactionManager(UserTransaction userTransaction,
                                                              UserTransactionManager userTransactionManager) {
        val tm = new JtaTransactionManager();
        tm.setUserTransaction(userTransaction);
        tm.setTransactionManager(userTransactionManager);
        tm.setGlobalRollbackOnParticipationFailure(true);
        tm.setRollbackOnCommitFailure(true);
        tm.setFailEarlyOnGlobalRollbackOnly(true);
        tm.setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_ALWAYS);
        return tm;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        userTransactionManager.setStartupTransactionService(true);
        return userTransactionManager;
    }

    @Bean
    public UserTransactionImp userTransaction() {
        return new UserTransactionImp();
    }
}
