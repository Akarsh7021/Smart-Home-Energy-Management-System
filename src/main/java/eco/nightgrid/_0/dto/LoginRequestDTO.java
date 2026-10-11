package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Credentials used to create an authenticated session.")
public record LoginRequestDTO(
		@Schema(description = "Registered account email address.", example = "homeowner@example.com")
		@NotBlank(message = "Email is required.")
		@Email(message = "Email must be valid.")
		String email,

		@Schema(description = "Plain-text password submitted over HTTPS.", example = "example-password")
		@NotBlank(message = "Password is required.")
		String password) {
}
