package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "DeviceControlRequest", description = "Command used to change a smart device's state.")
public record DeviceControlRequestDTO(
		@Schema(description = "Requested device state.", example = "ON", allowableValues = {"ON", "OFF"})
		@NotBlank(message = "A device command is required.")
		@Pattern(regexp = "(?i)ON|OFF", message = "Command must be ON or OFF.")
		String command) {
}
