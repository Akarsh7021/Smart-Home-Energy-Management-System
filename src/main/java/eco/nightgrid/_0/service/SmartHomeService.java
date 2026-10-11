package eco.nightgrid._0.service;

import eco.nightgrid._0.dao.DeviceDAO;
import eco.nightgrid._0.dao.DeviceReadingDAO;
import eco.nightgrid._0.dao.UserDAO;
import eco.nightgrid._0.dto.DashboardOverviewDTO;
import eco.nightgrid._0.dto.DashboardSummaryDTO;
import eco.nightgrid._0.dto.DeviceDTO;
import eco.nightgrid._0.dto.EnergySummaryDTO;
import eco.nightgrid._0.dto.UserDTO;
import eco.nightgrid._0.entity.DeviceReading;
import eco.nightgrid._0.entity.DeviceStatus;
import eco.nightgrid._0.entity.SmartDevice;
import eco.nightgrid._0.entity.User;
import eco.nightgrid._0.entity.UserRole;
import eco.nightgrid._0.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SmartHomeService {

	private static final ZoneId PROJECT_ZONE = ZoneId.of("America/Vancouver");
	private static final int ENERGY_SUMMARY_DAYS = 7;

	private final UserDAO userDAO;
	private final DeviceDAO deviceDAO;
	private final DeviceReadingDAO deviceReadingDAO;

	public SmartHomeService(
			UserDAO userDAO,
			DeviceDAO deviceDAO,
			DeviceReadingDAO deviceReadingDAO) {
		this.userDAO = userDAO;
		this.deviceDAO = deviceDAO;
		this.deviceReadingDAO = deviceReadingDAO;
	}

	public DashboardSummaryDTO getDashboardSummary(String userId) {
		User currentUser = findUser(userId);
		List<SmartDevice> devices = deviceDAO.findAll();
		List<DeviceReading> readings = loadWeeklyReadings();
		boolean canViewEnergy = currentUser.hasEnergyPermission();

		List<UserDTO> members = currentUser.getRole() == UserRole.HOMEOWNER
				? userDAO.findAllMembers().stream().map(this::toUserDTO).toList()
				: List.of();

		return new DashboardSummaryDTO(
				toUserDTO(currentUser),
				calculateOverview(devices, readings, canViewEnergy),
				toDeviceDTOs(devices, readings, canViewEnergy),
				members,
				canViewEnergy ? calculateEnergySummary(readings) : emptyEnergySummary());
	}

	public UserDTO getUser(String userId) {
		return toUserDTO(findUser(userId));
	}

	public List<DeviceDTO> getDevices(String userId) {
		User currentUser = findUser(userId);
		return toDeviceDTOs(
				deviceDAO.findAll(),
				loadWeeklyReadings(),
				currentUser.hasEnergyPermission());
	}

	@Transactional
	public DeviceDTO controlDevice(String userId, String deviceId, String command) {
		User currentUser = findUser(userId);
		if (!currentUser.hasControlPermission()) {
			throw new AccessDeniedException("Device-control permission is required.");
		}

		SmartDevice device = deviceDAO.findById(parseId(deviceId, "Device"))
				.orElseThrow(() -> new ResourceNotFoundException("Device was not found."));

		switch (command.trim().toUpperCase(Locale.ROOT)) {
			case "ON" -> device.turnOn();
			case "OFF" -> device.turnOff();
			default -> throw new IllegalArgumentException("Device command must be ON or OFF.");
		}

		SmartDevice savedDevice = deviceDAO.save(device);
		List<DeviceReading> readings = deviceReadingDAO.findByDeviceId(savedDevice.getDeviceId());
		return toDeviceDTO(savedDevice, readings, currentUser.hasEnergyPermission());
	}

	@Transactional
	public UserDTO setMemberPermissions(
			String ownerId,
			String memberId,
			boolean canControlDevices,
			boolean canViewEnergy) {
		User owner = findUser(ownerId);
		if (owner.getRole() != UserRole.HOMEOWNER) {
			throw new AccessDeniedException("Only a homeowner can update member permissions.");
		}

		User member = userDAO.findById(parseId(memberId, "Member"))
				.orElseThrow(() -> new ResourceNotFoundException("Member was not found."));
		if (member.getRole() != UserRole.FAMILY_MEMBER) {
			throw new IllegalArgumentException("Permissions can only be changed for a family member.");
		}

		member.setCanControlDevices(canControlDevices);
		member.setCanViewEnergy(canViewEnergy);
		return toUserDTO(userDAO.save(member));
	}

	private DashboardOverviewDTO calculateOverview(
			List<SmartDevice> devices,
			List<DeviceReading> readings,
			boolean canViewEnergy) {
		long activeDeviceCount = devices.stream()
				.filter(device -> device.getStatus() == DeviceStatus.ON)
				.count();

		double currentPowerWatts = 0;
		double todayEnergyKWh = 0;
		if (canViewEnergy) {
			Map<UUID, DeviceReading> latestReadings = latestReadingsByDevice(readings);
			currentPowerWatts = latestReadings.values().stream()
					.mapToDouble(reading -> readingValue(reading, "powerWatts"))
					.sum();

			LocalDate today = LocalDate.now(PROJECT_ZONE);
			todayEnergyKWh = readings.stream()
					.filter(reading -> readingDate(reading).equals(today))
					.mapToDouble(reading -> readingValue(reading, "energyKWh"))
					.sum();
		}

		String systemStatus;
		if (devices.isEmpty()) {
			systemStatus = "NO_DEVICES";
		} else if (readings.isEmpty()) {
			systemStatus = "NO_DATA";
		} else {
			systemStatus = "NORMAL";
		}

		return new DashboardOverviewDTO(
				Math.toIntExact(activeDeviceCount),
				devices.size(),
				currentPowerWatts,
				todayEnergyKWh,
				systemStatus);
	}

	private EnergySummaryDTO calculateEnergySummary(List<DeviceReading> readings) {
		LocalDate today = LocalDate.now(PROJECT_ZONE);
		Map<LocalDate, Double> totalsByDate = readings.stream()
				.collect(Collectors.groupingBy(
						this::readingDate,
						Collectors.summingDouble(reading -> readingValue(reading, "energyKWh"))));

		Map<String, Double> dailyUsage = new LinkedHashMap<>();
		for (int daysAgo = ENERGY_SUMMARY_DAYS - 1; daysAgo >= 0; daysAgo--) {
			LocalDate date = today.minusDays(daysAgo);
			String dayLabel = date.getDayOfWeek()
					.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
					.toUpperCase(Locale.ENGLISH);
			dailyUsage.put(dayLabel, totalsByDate.getOrDefault(date, 0.0));
		}

		double weekTotalKWh = dailyUsage.values().stream()
				.mapToDouble(Double::doubleValue)
				.sum();
		return new EnergySummaryDTO(
				totalsByDate.getOrDefault(today, 0.0),
				weekTotalKWh,
				dailyUsage);
	}

	private List<DeviceDTO> toDeviceDTOs(
			List<SmartDevice> devices,
			List<DeviceReading> readings,
			boolean canViewEnergy) {
		Map<UUID, List<DeviceReading>> readingsByDevice = readings.stream()
				.collect(Collectors.groupingBy(reading -> reading.getDevice().getDeviceId()));

		return devices.stream()
				.map(device -> toDeviceDTO(
						device,
						readingsByDevice.getOrDefault(device.getDeviceId(), List.of()),
						canViewEnergy))
				.toList();
	}

	private DeviceDTO toDeviceDTO(
			SmartDevice device,
			List<DeviceReading> readings,
			boolean canViewEnergy) {
		DeviceReading latestReading = readings.stream()
				.max(Comparator.comparing(DeviceReading::getRecordedAt))
				.orElse(null);
		LocalDate today = LocalDate.now(PROJECT_ZONE);

		double powerWatts = canViewEnergy && latestReading != null
				? readingValue(latestReading, "powerWatts")
				: 0.0;
		double energyKWh = canViewEnergy
				? readings.stream()
						.filter(reading -> readingDate(reading).equals(today))
						.mapToDouble(reading -> readingValue(reading, "energyKWh"))
						.sum()
				: 0.0;

		return new DeviceDTO(
				device.getDeviceId().toString(),
				device.getName(),
				device.getDeviceType(),
				device.getStatus().name(),
				powerWatts,
				energyKWh,
				latestReading == null ? null : latestReading.getRecordedAt());
	}

	private UserDTO toUserDTO(User user) {
		return new UserDTO(
				user.getUserId().toString(),
				user.getName(),
				user.getEmail(),
				user.getRole().name(),
				user.hasControlPermission(),
				user.hasEnergyPermission());
	}

	private Map<UUID, DeviceReading> latestReadingsByDevice(List<DeviceReading> readings) {
		return readings.stream().collect(Collectors.toMap(
				reading -> reading.getDevice().getDeviceId(),
				Function.identity(),
				(first, second) -> first.getRecordedAt().isAfter(second.getRecordedAt())
						? first
						: second));
	}

	private List<DeviceReading> loadWeeklyReadings() {
		LocalDate today = LocalDate.now(PROJECT_ZONE);
		OffsetDateTime startTime = today.minusDays(ENERGY_SUMMARY_DAYS - 1)
				.atStartOfDay(PROJECT_ZONE)
				.toOffsetDateTime();
		OffsetDateTime endTime = today.plusDays(1)
				.atStartOfDay(PROJECT_ZONE)
				.minusNanos(1)
				.toOffsetDateTime();
		return deviceReadingDAO.findAllByRecordedAtBetween(startTime, endTime);
	}

	private User findUser(String userId) {
		return userDAO.findById(parseId(userId, "User"))
				.orElseThrow(() -> new ResourceNotFoundException("User was not found."));
	}

	private UUID parseId(String value, String resourceName) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException exception) {
			throw new ResourceNotFoundException(resourceName + " was not found.");
		}
	}

	private LocalDate readingDate(DeviceReading reading) {
		return reading.getRecordedAt().atZoneSameInstant(PROJECT_ZONE).toLocalDate();
	}

	private double readingValue(DeviceReading reading, String fieldName) {
		Object value = reading.getReadingData().get(fieldName);
		return value instanceof Number number ? number.doubleValue() : 0.0;
	}

	private EnergySummaryDTO emptyEnergySummary() {
		Map<String, Double> dailyUsage = new LinkedHashMap<>();
		LocalDate today = LocalDate.now(PROJECT_ZONE);
		for (int daysAgo = ENERGY_SUMMARY_DAYS - 1; daysAgo >= 0; daysAgo--) {
			String dayLabel = today.minusDays(daysAgo).getDayOfWeek()
					.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
					.toUpperCase(Locale.ENGLISH);
			dailyUsage.put(dayLabel, 0.0);
		}
		return new EnergySummaryDTO(0.0, 0.0, dailyUsage);
	}
}
