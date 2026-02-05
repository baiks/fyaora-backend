package com.gler.assignment.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Fyaora assignment API",
                version = "1.0",
                description = "Fyaora assignment API",
                contact = @Contact(
                        name = "The interviewee Team",
                        email = "paulkabaiku023@gmail.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8016", description = "Local Development Server")
        }
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI();
    }
}
