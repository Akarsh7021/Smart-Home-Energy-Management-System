package eco.nightgrid._0.dto;

public record CsrfTokenDTO(
		String headerName,
		String parameterName,
		String token) {
}
