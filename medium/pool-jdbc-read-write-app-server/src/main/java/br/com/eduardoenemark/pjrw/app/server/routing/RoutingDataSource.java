package br.com.eduardoenemark.pjrw.app.server.routing;

import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DatasourceContext.get();
    }

    public static class DatasourceContext {
        private static final ThreadLocal<OperationType> CONTEXT = new ThreadLocal<>();

        public static void set(OperationType type) {
            CONTEXT.set(type);
        }

        public static OperationType get() {
            return CONTEXT.get() != null ? CONTEXT.get() : OperationType.READ;
        }

        public static void reset() {
            CONTEXT.remove();
        }
    }
}

