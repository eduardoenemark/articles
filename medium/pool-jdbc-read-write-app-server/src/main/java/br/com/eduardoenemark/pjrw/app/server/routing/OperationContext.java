package br.com.eduardoenemark.pjrw.app.server.routing;

import br.com.eduardoenemark.pjrw.app.server.operation.OperationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@ToString
public class OperationContext implements Serializable {

    private OperationType operationType;
    private long startOperationMillis;
    private long endOperationMillis;
    private String correlationId;

    public OperationContext() {
        this.setCorrelationId();
    }

    public void start(OperationType operationType) {
        this.operationType = operationType;
        this.startOperationMillis = System.currentTimeMillis();
    }

    public void end() {
        this.endOperationMillis = System.currentTimeMillis();
    }

    public long diffMillis() {
        return this.endOperationMillis - this.startOperationMillis;
    }

    public void setCorrelationId() {
        this.correlationId = UUID.randomUUID().toString();
    }

    public long getDiff() {
        return this.diffMillis();
    }
}
