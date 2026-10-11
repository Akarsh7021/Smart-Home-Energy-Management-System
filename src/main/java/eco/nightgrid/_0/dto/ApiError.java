package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "ApiError", description = "Structured details for an unsuccessful API operation.")
public record ApiError(
		@Schema(description = "Stable error identifier used by the frontend.", example = "VALIDATION_ERROR")
		String code,
		@Schema(description = "Optional technical explanation.", example = "Request validation failed.", nullable = true)
		String details,
		@Schema(description = "Validation messages keyed by request-field name.")
		Map<String, String> fieldErrors) {

	public ApiError {
		fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
	}
}
