package br.com.eduardenemark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class SampleResource {

    @GetMapping("/")
    public String index() {
        return "Hello World!";
    }

    @GetMapping("/now")
    public String now() {
        return String.format("This date-time now: %s.", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
