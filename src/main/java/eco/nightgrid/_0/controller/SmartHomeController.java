package eco.nightgrid._0.controller;

import eco.nightgrid._0.service.SmartHomeService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SmartHomeController {

	private final SmartHomeService smartHomeService;

	public SmartHomeController(SmartHomeService smartHomeService) {
		this.smartHomeService = smartHomeService;
	}
}
