package eco.nightgrid._0.dao;

import eco.nightgrid._0.entity.DeviceReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceReadingDAO extends JpaRepository<DeviceReading, UUID> {

	@Query("""
			select reading
			from DeviceReading reading
			where reading.device.deviceId = :deviceId
			order by reading.recordedAt desc
			""")
	List<DeviceReading> findByDeviceId(@Param("deviceId") UUID deviceId);

	@Query("""
			select reading
			from DeviceReading reading
			where reading.device.deviceId = :deviceId
			  and reading.recordedAt between :startTime and :endTime
			order by reading.recordedAt
			""")
	List<DeviceReading> findForDateRange(
			@Param("deviceId") UUID deviceId,
			@Param("startTime") OffsetDateTime startTime,
			@Param("endTime") OffsetDateTime endTime);

	List<DeviceReading> findAllByRecordedAtBetween(
			OffsetDateTime startTime,
			OffsetDateTime endTime);
}
