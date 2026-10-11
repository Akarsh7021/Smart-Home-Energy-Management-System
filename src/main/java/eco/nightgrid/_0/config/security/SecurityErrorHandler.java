package eco.nightgrid._0.config.security;

import eco.nightgrid._0.dto.ApiEnvelope;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public SecurityErrorHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		writeError(
				response,
				HttpServletResponse.SC_UNAUTHORIZED,
				ApiEnvelope.fail("UNAUTHENTICATED", "Please sign in to continue."));
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException exception) throws IOException, ServletException {
		writeError(
				response,
				HttpServletResponse.SC_FORBIDDEN,
				ApiEnvelope.fail(
						"ACCESS_DENIED",
						"You do not have permission to perform this action."));
	}

	private void writeError(
			HttpServletResponse response,
			int status,
			ApiEnvelope<Void> envelope) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), envelope);
	}
}
