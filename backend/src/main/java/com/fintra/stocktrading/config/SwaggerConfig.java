package com.fintra.stocktrading.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Value("${spring.application.name:Fintra Stock Trading API}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    @Value("${app.description:REST API for Fintra Stock Trading Platform}")
    private String applicationDescription;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.contact.name:Fintra Development Team}")
    private String contactName;

    @Value("${app.contact.email:dev@fintra.com.tr}")
    private String contactEmail;

    @Value("${app.contact.url:https://fintra.com.tr}")
    private String contactUrl;

    @Value("${app.license.name:MIT License}")
    private String licenseName;

    @Value("${app.license.url:https://opensource.org/licenses/MIT}")
    private String licenseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .addSecurityItem(createSecurityRequirement())
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, createSecurityScheme()));
    }

    private Info createApiInfo() {
        return new Info()
                .title(applicationName)
                .version(applicationVersion)
                .description(buildDescription())
                .contact(createContact())
                .license(createLicense());
    }

    private String buildDescription() {
        StringBuilder description = new StringBuilder(applicationDescription);
        return description.toString();
    }

    private Contact createContact() {
        return new Contact()
                .name(contactName)
                .email(contactEmail)
                .url(contactUrl);
    }

    private License createLicense() {
        return new License()
                .name(licenseName)
                .url(licenseUrl);
    }

    private List<Server> createServers() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Development Server");

        return List.of(devServer);
    }

    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList(BEARER_AUTH_SCHEME);
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token for API authentication. Format: Bearer {token}")
                .name(BEARER_AUTH_SCHEME);
    }
}
