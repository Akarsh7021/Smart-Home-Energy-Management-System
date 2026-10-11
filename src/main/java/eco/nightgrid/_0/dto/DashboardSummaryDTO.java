package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "DashboardSummary", description = "All information required to render the first-iteration dashboard.")
public record DashboardSummaryDTO(
		@Schema(description = "Currently authenticated user.")
		UserDTO currentUser,
		@Schema(description = "High-level device and energy totals.")
		DashboardOverviewDTO overview,
		@Schema(description = "Smart devices displayed on the dashboard.")
		List<DeviceDTO> devices,
		@Schema(description = "Family members visible to a homeowner; empty for other users.")
		List<UserDTO> members,
		@Schema(description = "Seven-day energy summary when the user has permission.")
		EnergySummaryDTO energy) {
}
