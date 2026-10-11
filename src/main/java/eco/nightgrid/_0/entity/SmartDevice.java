package eco.nightgrid._0.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "smart_devices")
public class SmartDevice {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "device_id", nullable = false, updatable = false)
	private UUID deviceId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "device_type", nullable = false, length = 50)
	private String deviceType;

	@Column(length = 100)
	private String manufacturer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DeviceStatus status;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@OneToMany(mappedBy = "device")
	private List<DeviceReading> readings = new ArrayList<>();

	protected SmartDevice() {
	}

	public SmartDevice(String name, String deviceType, String manufacturer, DeviceStatus status) {
		this.name = name;
		this.deviceType = deviceType;
		this.manufacturer = manufacturer;
		this.status = status;
	}

	public void turnOn() {
		status = DeviceStatus.ON;
	}

	public void turnOff() {
		status = DeviceStatus.OFF;
	}

	public UUID getDeviceId() {
		return deviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public List<DeviceReading> getReadings() {
		return readings;
	}
}
