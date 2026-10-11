package eco.nightgrid._0.service;

import eco.nightgrid._0.dao.DeviceDAO;
import eco.nightgrid._0.dao.DeviceReadingDAO;
import eco.nightgrid._0.dao.UserDAO;
import org.springframework.stereotype.Service;

@Service
public class SmartHomeService {

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
}
