package eco.nightgrid._0.dto;

public record DashboardOverviewDTO(
		int activeDeviceCount,
		int totalDeviceCount,
		double currentPowerWatts,
		double todayEnergyKWh,
		String systemStatus) {
}
