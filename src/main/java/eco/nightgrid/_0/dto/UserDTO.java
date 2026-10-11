package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "User", description = "User information safe to return to the frontend.")
public record UserDTO(
		@Schema(description = "User UUID.", example = "22222222-2222-2222-2222-222222222222")
		String userId,
		@Schema(description = "Display name.", example = "Alex Morgan")
		String name,
		@Schema(description = "Account email address.", example = "alex@example.com")
		String email,
		@Schema(description = "Household role.", example = "FAMILY_MEMBER", allowableValues = {"HOMEOWNER", "FAMILY_MEMBER"})
		String role,
		@Schema(description = "Whether the user may control devices.", example = "true")
		boolean canControlDevices,
		@Schema(description = "Whether the user may view energy information.", example = "true")
		boolean canViewEnergy) {
}
