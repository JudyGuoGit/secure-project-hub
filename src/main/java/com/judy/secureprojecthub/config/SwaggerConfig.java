package com.judy.secureprojecthub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for JWT Bearer Token authentication
 * 
 * All schemas are auto-generated from @Schema annotations on:
 * - Entities: User, Role, Permission, UserRole, RolePermission, AuditLog
 * - DTOs: TokenResponseDto
 * 
 * Entity schemas are registered via @Schema annotations on the classes
 * and will be auto-included when referenced in @ApiResponse content schemas.
 * 
 * This configuration handles:
 * - API metadata (title, version, description)
 * - Security scheme (Bearer token)
 * - Security requirement (apply to all endpoints)
 * 
 * The auth section allows users to:
 * 1. Generate a token via POST /api/token (username: admin, password: admin)
 * 2. Copy the token from the response
 * 3. Click "Authorize" button in Swagger UI
 * 4. Paste the token in the auth dialog
 * 5. All subsequent API calls will include the Bearer token in Authorization header
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            final String securitySchemeName = "bearerAuth";
            
            openApi.info(new Info()
                    .title("Secure Project Hub API")
                    .version("1.0.0")
                    .description("Secure Project Hub - OAuth2 JWT Bearer Token Authentication\n\n" +
                            "**How to use JWT Authentication:**\n" +
                            "1. Call POST `/api/token` with credentials (admin/admin)\n" +
                            "2. Copy the token from the response\n" +
                            "3. Click the 'Authorize' button (lock icon) at top-right\n" +
                            "4. Paste the token in the 'Value' field\n" +
                            "5. Click 'Authorize' to apply to all requests\n" +
                            "6. All subsequent API calls will include the Bearer token"));
            
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.components(components);
            }
            
            // Add security scheme for Bearer token auth
            components.addSecuritySchemes(securitySchemeName, 
                    new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter your JWT token here. Get one from POST /api/token endpoint.\n\n" +
                                    "Test credentials:\n" +
                                    "- username: admin\n" +
                                    "- password: admin\n\n" +
                                    "Or use:\n" +
                                    "- username: user, password: password\n" +
                                    "- username: john, password: password\n" +
                                    "- username: jane, password: password"));
            
            // Apply security requirement to all endpoints
            openApi.addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
        };
    }
}

