package eco.nightgrid._0.dto;

import java.util.Map;

public record ApiEnvelope<T>(
		boolean success,
		String message,
		T data,
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
