package com.helenaramyan.cardauth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI cardAuthOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Card Authorization Simulator")
                .version("v1")
                .description("Issuer-side authorization decisions with ISO 8583 response codes. "
                        + "Amounts are in minor units; cards are addressed by token, never by PAN.")
                .contact(new Contact().name("Helen Aramyan").url("https://helenarams.github.io"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
