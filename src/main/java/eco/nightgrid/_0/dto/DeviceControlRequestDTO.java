package eco.nightgrid._0.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeviceControlRequestDTO(
		@NotBlank(message = "A device command is required.")
		@Pattern(regexp = "(?i)ON|OFF", message = "Command must be ON or OFF.")
		String command) {
}
