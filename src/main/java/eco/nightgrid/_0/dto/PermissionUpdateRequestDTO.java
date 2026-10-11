package eco.nightgrid._0.dto;

import jakarta.validation.constraints.NotNull;

public record PermissionUpdateRequestDTO(
		@NotNull(message = "Device-control permission is required.")
		Boolean canControlDevices,

		@NotNull(message = "Energy-view permission is required.")
		Boolean canViewEnergy) {
}
