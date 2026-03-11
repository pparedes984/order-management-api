package com.example.ordersmanagement.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Orders Management API.
 * 
 * This configuration provides global API documentation including:
 * - API information (title, version, description)
 * - Contact and license details
 * - Server configurations for different environments
 * - External documentation links
 * 
 * The Swagger UI is accessible at: /swagger-ui/index.html
 * The OpenAPI JSON is accessible at: /v3/api-docs
 * 
 * @author Paul Paredes
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI documentation bean.
     * 
     * @return OpenAPI configuration with complete API metadata
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orders Management API")
                        .version("1.0.0")
                        .description("""
                                REST API for comprehensive order management system.
                                
                                This API provides endpoints for:
                                - Customer management (CRUD operations)
                                - Product inventory management
                                - Order creation and tracking
                                - Order cancellation with stock restoration
                                
                                All endpoints return standardized error responses with specific error codes
                                for easy error handling and debugging.
                                """)
                        .contact(new Contact()
                                .name("Paul Paredes")
                                .email("paulparedesq@gmail.com")
                                .url("https://github.com/pparedes984"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.ordersmanagement.com")
                                .description("Production Server")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Error Codes Documentation")
                        .url("https://github.com/paulparedes/ordersmanagement/blob/main/docs/ERROR_CODES.md"));
    }
}
