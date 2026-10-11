package eco.nightgrid._0.dto;

import java.time.OffsetDateTime;

public record DeviceDTO(
		String deviceId,
		String name,
		String type,
		String status,
		double powerWatts,
		double energyKWh,
		OffsetDateTime lastActive) {
}
