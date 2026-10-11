package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DashboardOverview", description = "Summary cards displayed at the top of the dashboard.")
public record DashboardOverviewDTO(
		@Schema(description = "Number of devices currently on.", example = "1")
		int activeDeviceCount,
		@Schema(description = "Number of registered smart devices.", example = "3")
		int totalDeviceCount,
		@Schema(description = "Combined latest power demand in watts.", example = "8.4")
		double currentPowerWatts,
		@Schema(description = "Energy recorded today in kilowatt-hours.", example = "0.18")
		double todayEnergyKWh,
		@Schema(description = "Overall system condition.", example = "NORMAL", allowableValues = {"NORMAL", "NO_DATA", "NO_DEVICES"})
		String systemStatus) {
}
