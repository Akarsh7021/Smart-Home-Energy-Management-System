package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CsrfToken", description = "CSRF information used by the React API client for state-changing requests.")
public record CsrfTokenDTO(
		@Schema(description = "API cookie holding the server-side token value.", example = "XSRF-TOKEN")
		String cookieName,
		@Schema(description = "Request header that must carry the returned masked token.", example = "X-XSRF-TOKEN")
		String headerName,
		@Schema(description = "Masked token value to send in the request header.")
		String token) {
}
