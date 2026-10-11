package eco.nightgrid._0.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "device_readings")
public class DeviceReading {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "reading_id", nullable = false, updatable = false)
	private UUID readingId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "device_id", nullable = false)
	private SmartDevice device;

	@Column(name = "recorded_at", nullable = false)
	private OffsetDateTime recordedAt;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "reading_data", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> readingData = new HashMap<>();

	protected DeviceReading() {
	}

	public DeviceReading(SmartDevice device, OffsetDateTime recordedAt,
			Map<String, Object> readingData) {
		this.device = device;
		this.recordedAt = recordedAt;
		this.readingData = readingData;
	}

	public UUID getReadingId() {
		return readingId;
	}

	public SmartDevice getDevice() {
		return device;
	}

	public void setDevice(SmartDevice device) {
		this.device = device;
	}

	public OffsetDateTime getRecordedAt() {
		return recordedAt;
	}

	public void setRecordedAt(OffsetDateTime recordedAt) {
		this.recordedAt = recordedAt;
	}

	public Map<String, Object> getReadingData() {
		return readingData;
	}

	public void setReadingData(Map<String, Object> readingData) {
		this.readingData = readingData;
	}
}
