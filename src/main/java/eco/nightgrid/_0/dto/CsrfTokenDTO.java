package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CsrfToken", description = "Cookie and header names used by the React API client for CSRF protection.")
public record CsrfTokenDTO(
		@Schema(description = "Browser cookie containing the token value.", example = "XSRF-TOKEN")
		String cookieName,
		@Schema(description = "Request header that must carry the cookie value.", example = "X-XSRF-TOKEN")
		String headerName) {
}
