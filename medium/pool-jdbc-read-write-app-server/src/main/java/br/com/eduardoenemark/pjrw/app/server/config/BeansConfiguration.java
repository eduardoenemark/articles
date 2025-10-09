package br.com.eduardoenemark.pjrw.app.server.config;

import br.com.eduardoenemark.pjrw.app.server.repository.ProductRepository;
import br.com.eduardoenemark.pjrw.app.server.service.ProductService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfiguration {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("br.com.eduardoenemark.pjrw.app.server");

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
