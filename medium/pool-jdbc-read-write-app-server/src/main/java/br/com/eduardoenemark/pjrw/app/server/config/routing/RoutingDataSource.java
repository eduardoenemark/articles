package br.com.eduardoenemark.pjrw.app.server.config.routing;

import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DatasourceContext.get();
    }

    public static class DatasourceContext {
        private static final ThreadLocal<OperationType> context = new ThreadLocal<>();

        public static void set(OperationType type) {
            context.set(type);
        }

        public static OperationType get() {
            return context.get() != null ? context.get() : OperationType.READ;
        }

        public static void reset() {
            context.remove();
        }
    }
}

