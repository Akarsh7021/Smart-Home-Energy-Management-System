package eco.nightgrid._0.dao;

import eco.nightgrid._0.entity.SmartDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceDAO extends JpaRepository<SmartDevice, UUID> {
}
