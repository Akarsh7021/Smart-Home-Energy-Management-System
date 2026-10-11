package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CsrfToken", description = "CSRF token information used by the React API client.")
public record CsrfTokenDTO(
		@Schema(description = "Request header that must carry the token.", example = "X-XSRF-TOKEN")
		String headerName,
		@Schema(description = "Alternative form parameter name.", example = "_csrf")
		String parameterName,
		@Schema(description = "Token value to send with state-changing requests.")
		String token) {
}
