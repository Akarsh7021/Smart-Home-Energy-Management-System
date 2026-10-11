package eco.nightgrid._0.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(info = @Info(
		title = "NightGrid Smart Home API",
		version = "1.0",
		description = "REST API for session authentication, dashboard summaries, device control, and household member permissions."))
@SecurityScheme(
		name = "sessionCookie",
		type = SecuritySchemeType.APIKEY,
		in = SecuritySchemeIn.COOKIE,
		paramName = "NIGHTGRID_SESSION",
		description = "Database-backed session cookie created after a successful login.")
@SecurityScheme(
		name = "csrfToken",
		type = SecuritySchemeType.APIKEY,
		in = SecuritySchemeIn.HEADER,
		paramName = "X-XSRF-TOKEN",
		description = "Value copied from the XSRF-TOKEN cookie after calling GET /api/auth/csrf. Required for POST, PATCH, and DELETE requests.")
public class OpenApiConfig {
}
