package eco.nightgrid._0.exception;

import eco.nightgrid._0.dto.ApiEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiEnvelope<Void>> handleValidationError(
			MethodArgumentNotValidException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}

		return ResponseEntity.badRequest().body(ApiEnvelope.fail(
				"VALIDATION_ERROR",
				"Please correct the highlighted fields.",
				"Request validation failed.",
				fieldErrors));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiEnvelope<Void>> handleAuthenticationError(
			AuthenticationException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiEnvelope.fail(
				"AUTHENTICATION_FAILED",
				"The email or password is incorrect."));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiEnvelope<Void>> handleAccessDenied(AccessDeniedException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiEnvelope.fail(
				"ACCESS_DENIED",
				"You do not have permission to perform this action."));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiEnvelope<Void>> handleNotFound(ResourceNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiEnvelope.fail(
				"RESOURCE_NOT_FOUND",
				exception.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiEnvelope<Void>> handleUnexpectedError(Exception exception) {
		LOGGER.error("Unexpected application error", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiEnvelope.fail(
				"INTERNAL_SERVER_ERROR",
				"An unexpected error occurred."));
	}
}
