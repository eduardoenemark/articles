package br.com.eduardoenemark.pjrw.app.server.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Configuration
public class PropsConfig {

    @Setter
    @Getter
    @ToString
    @Configuration
    @ConfigurationProperties(prefix = "datasource")
    public static class DataSourcePropsConfig {

        String url;
        String driverClassName;
        String username;
        String password;

        @Setter
        @Getter
        @ToString
        @FieldDefaults(level = AccessLevel.PROTECTED)
        public static class PoolPropsConfig {
            String name;
            int minimumSize;
            int maximumSize;
            int minimumIdle;
            int borrowConnectionTimeoutSecs;
            int maxIdleTimeSecs;
            int initializationFailTimeoutSecs;
            String connectionTestQuery;
            boolean readOnly;
        }

        @Setter
        @Getter
        @ToString
        @FieldDefaults(level = AccessLevel.PROTECTED)
        public static class HibernatePropsConfig {
            boolean showSql;
            boolean formatSql;
            String dialect;
            String defaultSchema;
            boolean generateStatistics;
            String hbm2ddlAuto;
            boolean connectionProviderDisablesAutocommit;
            boolean connectionAutocommit;
            String connectionReleaseMode;
            boolean jpaComplianceTransaction;
            boolean jpaComplianceClosed;
            boolean transactionFlushBeforeCompletion;
            boolean transactionAutoCloseSession;
        }

        @ToString
        @Configuration
        public static class PoolReadPropsConfig {

            @Bean(name = "poolReadPropsConfig")
            @ConfigurationProperties(prefix = "datasource.pool.read")
            public PoolPropsConfig poolReadPropsConfig() {
                return new PoolPropsConfig();
            }
        }

        @ToString
        @Configuration
        public static class PoolWritePropsConfig {

            @Bean(name = "poolWritePropsConfig")
            @ConfigurationProperties(prefix = "datasource.pool.write")
            public PoolPropsConfig poolWritePropsConfig() {
                return new PoolPropsConfig();
            }
        }
    }

    @Setter
    @Getter
    @ToString
    @Configuration
    @ConfigurationProperties(prefix = "swagger")
    public static class SwaggerConfig {
        String title;
        String description;
        String version;

        @Setter
        @Getter
        @ToString
        @Configuration
        @ConfigurationProperties(prefix = "swagger.contact")
        public static class ContactConfig {
            String name;
            String email;
        }
    }
}
