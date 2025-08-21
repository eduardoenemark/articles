package br.com.eduardoenemark.pjrw.app.server.config;

import br.com.eduardoenemark.pjrw.app.server.entity.Product;
import br.com.eduardoenemark.pjrw.app.server.intercept.RequestDurationInterceptor;
import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import br.com.eduardoenemark.pjrw.app.server.repository.ProductRepository;
import br.com.eduardoenemark.pjrw.app.server.routing.RoutingDataSource;
import br.com.eduardoenemark.pjrw.app.server.routing.RoutingPlatformTransactionManager;
import br.com.eduardoenemark.pjrw.app.server.service.ProductService;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosNonXADataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.val;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "br.com.eduardoenemark.pjrw.app.server.repository")
public class AppConfiguration {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AppConfiguration.class.getPackage().getName());

    public static final String READ_TRANSACTION_MANAGER = "readTransactionManager";
    public static final String WRITE_TRANSACTION_MANAGER = "writeTransactionManager";

    public static final String READ_DATASOURCE = "readDataSource";
    public static final String WRITE_DATASOURCE = "writeDataSource";
    public static final String ROUTING_DATASOURCE = "routingDataSource";

    public static final String READ_HIBERNATE = "readHibernate";
    public static final String WRITE_HIBERNATE = "writeHibernate";

    public static final String READ_ENTITY_MANAGER_FACTORY = "readEntityManagerFactory";
    public static final String WRITE_ENTITY_MANAGER_FACTORY = "writeEntityManagerFactory";

    @Configuration
    public static class WebConfig implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new RequestDurationInterceptor())
                    .addPathPatterns("/**");
        }
    }


    @Bean(name = READ_DATASOURCE)
    public DataSource readDataSource(PropsConfig.DataSourcePropsConfig dataSourcePropsConfig,
                                     PropsConfig.DataSourcePropsConfig.PoolPropsConfig poolReadPropsConfig) {
        val dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourcePropsConfig.getUrl());
        dataSource.setDriverClassName(dataSourcePropsConfig.getDriverClassName());
        dataSource.setUsername(dataSourcePropsConfig.getUsername());
        dataSource.setPassword(dataSourcePropsConfig.getPassword());
        dataSource.setMinimumIdle(poolReadPropsConfig.getMinimumIdle());
        dataSource.setMaximumPoolSize(poolReadPropsConfig.getMaximumSize());
        dataSource.setMinimumIdle(poolReadPropsConfig.getMinimumSize());
        dataSource.setConnectionTimeout(poolReadPropsConfig.getBorrowConnectionTimeoutSecs() * 1000L); // Convert to milliseconds
        dataSource.setIdleTimeout(poolReadPropsConfig.getMaxIdleTimeSecs() * 1000L); // Convert to milliseconds
        dataSource.setInitializationFailTimeout(poolReadPropsConfig.getInitializationFailTimeoutSecs() * 1000L); // Convert to milliseconds
        dataSource.setConnectionTestQuery(poolReadPropsConfig.getConnectionTestQuery());
        dataSource.setPoolName(poolReadPropsConfig.getName());
        dataSource.setAutoCommit(poolReadPropsConfig.getAutocommit());
        dataSource.setReadOnly(poolReadPropsConfig.getReadOnly());
        return dataSource;
    }

    @Bean(name = WRITE_DATASOURCE)
    public DataSource writeDataSource(PropsConfig.DataSourcePropsConfig dataSourcePropsConfig,
                                      PropsConfig.DataSourcePropsConfig.PoolPropsConfig poolWritePropsConfig) throws SQLException {
        val dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUrl(dataSourcePropsConfig.getUrl());
        dataSource.setUser(dataSourcePropsConfig.getUsername());
        dataSource.setPassword(dataSourcePropsConfig.getPassword());
        dataSource.setDriverClassName(dataSourcePropsConfig.getDriverClassName());
        dataSource.setUniqueResourceName(poolWritePropsConfig.getName());
        dataSource.setLoginTimeout(poolWritePropsConfig.getInitializationFailTimeoutSecs());
        dataSource.setMaxPoolSize(poolWritePropsConfig.getMaximumSize());
        dataSource.setMinPoolSize(poolWritePropsConfig.getMinimumSize());
        dataSource.setBorrowConnectionTimeout(poolWritePropsConfig.getBorrowConnectionTimeoutSecs() * 1000); // Convert to milliseconds
        dataSource.setMaxIdleTime(poolWritePropsConfig.getMaxIdleTimeSecs() * 1000); // Convert to milliseconds
        dataSource.setTestQuery(poolWritePropsConfig.getConnectionTestQuery());
        dataSource.setReadOnly(poolWritePropsConfig.getReadOnly());
        dataSource.setLocalTransactionMode(poolWritePropsConfig.getAutocommit());
        dataSource.setIgnoreJtaTransactions(true);
        return dataSource;
    }

    @Primary
    @Bean(name = {ROUTING_DATASOURCE, "dataSource"})
    public AbstractRoutingDataSource routingDataSource(@Qualifier(READ_DATASOURCE) DataSource readDataSource,
                                                       @Qualifier(WRITE_DATASOURCE) DataSource writeDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(
                Collections.unmodifiableMap(
                        new HashMap<OperationType, DataSource>() {{
                            put(OperationType.READ, readDataSource);
                            put(OperationType.WRITE, writeDataSource);
                        }}));
        routingDataSource.setDefaultTargetDataSource(writeDataSource); // Fallback
        return routingDataSource;
    }

    @Primary
    @Bean(name = READ_HIBERNATE)
    @ConfigurationProperties("datasource.pool.read")
    public Properties readHibernate() {
        return new Properties();
    }

    @Bean(name = WRITE_HIBERNATE)
    @ConfigurationProperties("datasource.pool.write")
    public Properties writeHibernate() {
        return new Properties();
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        userTransactionManager.setStartupTransactionService(true);
        return userTransactionManager;
    }

    @Bean
    public UserTransaction userTransaction() {
        return new UserTransactionImp();
    }

    private LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                        Properties hibernateProperties) {
        val em = new LocalContainerEntityManagerFactoryBean();
        val vendor = new HibernateJpaVendorAdapter();
        em.setDataSource(dataSource);
        em.setPackagesToScan(Product.class.getPackage().getName());
        em.setJpaVendorAdapter(vendor);
        em.setJpaProperties(hibernateProperties);
        return em;
    }

    @Primary
    @Bean(name = {READ_ENTITY_MANAGER_FACTORY, "entityManagerFactory"})
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(@Qualifier(ROUTING_DATASOURCE) DataSource dataSource,
                                                                           @Qualifier(READ_HIBERNATE) Properties hibernateProperties) {
        return this.entityManagerFactory(dataSource, hibernateProperties);
    }

    @Bean(name = {WRITE_ENTITY_MANAGER_FACTORY})
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(@Qualifier(ROUTING_DATASOURCE) DataSource dataSource,
                                                                            @Qualifier(WRITE_HIBERNATE) Properties hibernateProperties) {
        return this.entityManagerFactory(dataSource, hibernateProperties);
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

    @Bean
    public OpenAPI openApi(PropsConfig.SwaggerConfig swaggerConfig,
                           PropsConfig.SwaggerConfig.ContactConfig contactConfig) {
        return new OpenAPI()
                .info(new Info()
                        .title(swaggerConfig.getTitle())
                        .description(swaggerConfig.getDescription())
                        .version(swaggerConfig.getVersion())
                        .contact(new Contact()
                                .name(contactConfig.getName())
                                .email(contactConfig.getEmail())));
    }

    @Bean
    public ProductService productService(ProductRepository repository) {
        return new ProductService(repository);
    }
}
