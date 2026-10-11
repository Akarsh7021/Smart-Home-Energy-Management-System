package eco.nightgrid._0.controller;

import eco.nightgrid._0.config.security.AuthenticatedUser;
import eco.nightgrid._0.dto.ApiEnvelope;
import eco.nightgrid._0.dto.DashboardSummaryDTO;
import eco.nightgrid._0.dto.LoginRequestDTO;
import eco.nightgrid._0.dto.UserDTO;
import eco.nightgrid._0.service.SmartHomeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SmartHomeController {

	private final SmartHomeService smartHomeService;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;

	public SmartHomeController(
			SmartHomeService smartHomeService,
			AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository) {
		this.smartHomeService = smartHomeService;
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
	}

	@PostMapping("/auth/login")
	public ApiEnvelope<UserDTO> handleLogin(
			@Valid @RequestBody LoginRequestDTO loginRequest,
			HttpServletRequest request,
			HttpServletResponse response) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(
						loginRequest.email(),
						loginRequest.password()));

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);
		securityContextRepository.saveContext(securityContext, request, response);

		AuthenticatedUser currentUser = requireAuthenticatedUser(authentication);
		return ApiEnvelope.ok(
				smartHomeService.getUser(currentUser.userId().toString()),
				"Signed in successfully.");
	}

	@PostMapping("/auth/logout")
	public ApiEnvelope<Void> handleLogout(
			Authentication authentication,
			HttpServletRequest request,
			HttpServletResponse response) {
		new SecurityContextLogoutHandler().logout(request, response, authentication);
		new CookieClearingLogoutHandler("NIGHTGRID_SESSION", "XSRF-TOKEN")
				.logout(request, response, authentication);
		return ApiEnvelope.ok(null, "Signed out successfully.");
	}

	@GetMapping("/dashboard")
	public ApiEnvelope<DashboardSummaryDTO> getDashboard(Authentication authentication) {
		AuthenticatedUser currentUser = requireAuthenticatedUser(authentication);
		return ApiEnvelope.ok(
				smartHomeService.getDashboardSummary(currentUser.userId().toString()));
	}

	private AuthenticatedUser requireAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new IllegalStateException("Authenticated user details are unavailable.");
		}
		return user;
	}
}
