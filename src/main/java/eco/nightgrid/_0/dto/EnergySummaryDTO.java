package eco.nightgrid._0.dto;

import java.util.Map;

public record EnergySummaryDTO(
		double todayKWh,
		double weekTotalKWh,
		Map<String, Double> dailyUsage) {
}
