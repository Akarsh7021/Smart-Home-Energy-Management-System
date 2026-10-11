package eco.nightgrid._0.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "Device", description = "Smart-device state and its latest permitted energy information.")
public record DeviceDTO(
		@Schema(description = "Device UUID.", example = "11111111-1111-1111-1111-111111111111")
		String deviceId,
		@Schema(description = "User-facing device name.", example = "Living room light")
		String name,
		@Schema(description = "Device category.", example = "Smart light")
		String type,
		@Schema(description = "Current operating state.", example = "ON", allowableValues = {"ON", "OFF"})
		String status,
		@Schema(description = "Latest power demand in watts.", example = "8.4")
		double powerWatts,
		@Schema(description = "Energy recorded today in kilowatt-hours.", example = "0.18")
		double energyKWh,
		@Schema(description = "Time of the latest device reading.", example = "2026-10-10T20:30:00-07:00")
		OffsetDateTime lastActive) {
}
