package br.com.eduardoenemark.pjrw.app.server.config;

import br.com.eduardoenemark.pjrw.app.server.config.routing.RoutingDataSource;
import br.com.eduardoenemark.pjrw.app.server.entity.Product;
import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import com.atomikos.jdbc.AtomikosNonXADataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.eduardoenemark.pjrw.app.server.repository")
public class DataSourceBeansConfiguration {

    public static final String READ_DATASOURCE = "readDataSource";
    public static final String WRITE_DATASOURCE = "writeDataSource";
    public static final String ROUTING_DATASOURCE = "routingDataSource";

    public static final String READ_HIBERNATE = "readHibernate";
    public static final String WRITE_HIBERNATE = "writeHibernate";

    public static final String READ_ENTITY_MANAGER_FACTORY = "readEntityManagerFactory";
    public static final String WRITE_ENTITY_MANAGER_FACTORY = "writeEntityManagerFactory";

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
        dataSource.setPoolName(poolReadPropsConfig.getName());
        /* AutoCommit is ignored because:
         * o.s.jdbc.datasource.DataSourceUtils - Could not reset JDBC Connection after transaction
         * org.postgresql.util.PSQLException: Cannot change transaction read-only property in the middle of a transaction.
         * at org.postgresql.jdbc.PgConnection.setReadOnly(PgConnection.java:916)
         */
        //dataSource.setAutoCommit(false);
        dataSource.setReadOnly(true);
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
        dataSource.setIgnoreJtaTransactions(true);
        dataSource.setReadOnly(false);
        dataSource.setLocalTransactionMode(true);
        return dataSource;
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
}
