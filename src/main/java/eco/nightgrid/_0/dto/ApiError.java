package eco.nightgrid._0.dto;

import java.util.Map;

public record ApiError(
		String code,
		String details,
		Map<String, String> fieldErrors) {

	public ApiError {
		fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
	}
}
