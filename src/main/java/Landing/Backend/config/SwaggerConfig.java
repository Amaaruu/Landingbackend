package Landing.Backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LAndIng API")
                        .version("1.0")
                        .description("API REST Backend – Proyecto Landing con IA")
                        .contact(new Contact()
                                .name("lAndIng Team")));
    }
}