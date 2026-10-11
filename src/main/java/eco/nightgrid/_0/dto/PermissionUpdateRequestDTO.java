package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "PermissionUpdateRequest", description = "Permissions a homeowner assigns to a family member.")
public record PermissionUpdateRequestDTO(
		@Schema(description = "Whether the member may turn smart devices on or off.", example = "true")
		@NotNull(message = "Device-control permission is required.")
		Boolean canControlDevices,

		@Schema(description = "Whether the member may view household energy information.", example = "true")
		@NotNull(message = "Energy-view permission is required.")
		Boolean canViewEnergy) {
}
