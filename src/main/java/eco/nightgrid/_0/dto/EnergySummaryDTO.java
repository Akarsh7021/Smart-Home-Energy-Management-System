package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "EnergySummary", description = "Energy totals for today and the most recent seven days.")
public record EnergySummaryDTO(
		@Schema(description = "Energy recorded today in kilowatt-hours.", example = "0.18")
		double todayKWh,
		@Schema(description = "Combined seven-day usage in kilowatt-hours.", example = "5.48")
		double weekTotalKWh,
		@Schema(description = "Usage grouped by abbreviated day name.", example = "{\"MON\": 0.65, \"TUE\": 0.9}")
		Map<String, Double> dailyUsage) {
}
