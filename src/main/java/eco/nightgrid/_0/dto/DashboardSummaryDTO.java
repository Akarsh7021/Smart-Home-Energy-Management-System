package eco.nightgrid._0.dto;

import java.util.List;

public record DashboardSummaryDTO(
		UserDTO currentUser,
		DashboardOverviewDTO overview,
		List<DeviceDTO> devices,
		List<UserDTO> members,
		EnergySummaryDTO energy) {
}
