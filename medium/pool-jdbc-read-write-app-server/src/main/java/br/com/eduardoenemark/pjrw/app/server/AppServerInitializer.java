package br.com.eduardoenemark.pjrw.app.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(
        scanBasePackages = {
                "br.com.eduardoenemark.pjrw.app.server.config",
                "br.com.eduardoenemark.pjrw.app.server.repository",
                "br.com.eduardoenemark.pjrw.app.server.resource",
                "br.com.eduardoenemark.pjrw.app.server.routing",
                "br.com.eduardoenemark.pjrw.app.server.service"},
        exclude = {DataSourceAutoConfiguration.class})
public class AppServerInitializer extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AppServerInitializer.class, args);
    }
}
