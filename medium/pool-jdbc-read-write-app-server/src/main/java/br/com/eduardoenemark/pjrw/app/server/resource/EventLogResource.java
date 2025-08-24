package br.com.eduardoenemark.pjrw.app.server.resource;

import br.com.eduardoenemark.pjrw.app.server.event.OperationContextEvent;
import br.com.eduardoenemark.pjrw.app.server.event.OperationContextListener;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventLogResource {

    @GetMapping("/event/logs/operation")
    public ResponseEntity<List<OperationContextEvent>> generate() {
        return ResponseEntity.ok(OperationContextListener.getEvents());
    }
}
