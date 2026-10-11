package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "ApiEnvelope", description = "Standard structure returned by every NightGrid API operation.")
public record ApiEnvelope<T>(
		@Schema(description = "Whether the operation completed successfully.", example = "true")
		boolean success,
		@Schema(description = "Optional user-facing status message.", example = "Signed in successfully.", nullable = true)
		String message,
		@Schema(description = "Operation result; null when the operation fails.", nullable = true)
		T data,
		@Schema(description = "Error details; null when the operation succeeds.", nullable = true)
		ApiError error) {

	public static <T> ApiEnvelope<T> ok(T data) {
		return new ApiEnvelope<>(true, null, data, null);
	}

	public static <T> ApiEnvelope<T> ok(T data, String message) {
		return new ApiEnvelope<>(true, message, data, null);
	}

	public static <T> ApiEnvelope<T> fail(String code, String message) {
		return fail(code, message, null, Map.of());
	}

	public static <T> ApiEnvelope<T> fail(
			String code,
			String message,
			String details,
			Map<String, String> fieldErrors) {
		return new ApiEnvelope<>(
				false,
				message,
				null,
				new ApiError(code, details, fieldErrors));
	}
}
