package eco.nightgrid._0.controller;

import eco.nightgrid._0.config.security.AuthenticatedUser;
import eco.nightgrid._0.dto.ApiEnvelope;
import eco.nightgrid._0.dto.CsrfTokenDTO;
import eco.nightgrid._0.dto.DashboardSummaryDTO;
import eco.nightgrid._0.dto.DeviceControlRequestDTO;
import eco.nightgrid._0.dto.DeviceDTO;
import eco.nightgrid._0.dto.LoginRequestDTO;
import eco.nightgrid._0.dto.PermissionUpdateRequestDTO;
import eco.nightgrid._0.dto.UserDTO;
import eco.nightgrid._0.service.SmartHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api")
@Tag(name = "Smart Home", description = "Authentication, dashboard, device, and member-permission operations.")
public class SmartHomeController {

	private final SmartHomeService smartHomeService;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

	public SmartHomeController(
			SmartHomeService smartHomeService,
			AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		this.smartHomeService = smartHomeService;
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}

	@GetMapping("/auth/csrf")
	@Operation(
			summary = "Get a CSRF token",
			description = "Creates or refreshes the XSRF-TOKEN cookie. The client copies that cookie value into the X-XSRF-TOKEN header for state-changing requests.")
	@ApiResponse(responseCode = "200", description = "CSRF cookie created or refreshed successfully.")
	public ApiEnvelope<CsrfTokenDTO> getCsrfToken(
			@Parameter(hidden = true) CsrfToken csrfToken) {
		// Accessing the deferred token causes Spring Security to write its raw value
		// to the XSRF-TOKEN cookie. The value returned by getToken() is masked and
		// must not be used as the SPA's X-XSRF-TOKEN request header.
		csrfToken.getToken();
		return ApiEnvelope.ok(new CsrfTokenDTO(
				"XSRF-TOKEN",
				csrfToken.getHeaderName()));
	}

	@PostMapping("/auth/login")
	@Operation(
			summary = "Sign in",
			description = "Validates the email and password, then creates a database-backed session.",
			security = @SecurityRequirement(name = "csrfToken"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User authenticated and session created."),
			@ApiResponse(responseCode = "400", description = "Request validation failed."),
			@ApiResponse(responseCode = "401", description = "Email or password is incorrect."),
			@ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid.")
	})
	public ApiEnvelope<UserDTO> handleLogin(
			@Valid @RequestBody LoginRequestDTO loginRequest,
			HttpServletRequest request,
			HttpServletResponse response) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(
						loginRequest.email(),
						loginRequest.password()));
		sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

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
	@Operation(
			summary = "Sign out",
			description = "Invalidates the current session and clears the session and CSRF cookies.",
			security = {
					@SecurityRequirement(name = "sessionCookie"),
					@SecurityRequirement(name = "csrfToken")
			})
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Session ended successfully."),
			@ApiResponse(responseCode = "401", description = "No authenticated session exists."),
			@ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid.")
	})
	public ApiEnvelope<Void> handleLogout(
			@Parameter(hidden = true) Authentication authentication,
			HttpServletRequest request,
			HttpServletResponse response) {
		new SecurityContextLogoutHandler().logout(request, response, authentication);
		new CookieClearingLogoutHandler("NIGHTGRID_SESSION", "XSRF-TOKEN")
				.logout(request, response, authentication);
		return ApiEnvelope.ok(null, "Signed out successfully.");
	}

	@GetMapping("/dashboard")
	@Operation(
			summary = "Get the dashboard summary",
			description = "Returns the signed-in user, overview totals, devices, permitted member data, and seven-day energy usage.",
			security = @SecurityRequirement(name = "sessionCookie"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Dashboard summary returned successfully."),
			@ApiResponse(responseCode = "401", description = "No authenticated session exists.")
	})
	public ApiEnvelope<DashboardSummaryDTO> getDashboard(
			@Parameter(hidden = true) Authentication authentication) {
		AuthenticatedUser currentUser = requireAuthenticatedUser(authentication);
		return ApiEnvelope.ok(
				smartHomeService.getDashboardSummary(currentUser.userId().toString()));
	}

	@PatchMapping("/devices/{deviceId}/state")
	@PreAuthorize("hasAuthority('CONTROL_DEVICES')")
	@Operation(
			summary = "Control a smart device",
			description = "Turns one smart device on or off when the signed-in user has device-control permission.",
			security = {
					@SecurityRequirement(name = "sessionCookie"),
					@SecurityRequirement(name = "csrfToken")
			})
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Device state updated successfully."),
			@ApiResponse(responseCode = "400", description = "The command is invalid."),
			@ApiResponse(responseCode = "401", description = "No authenticated session exists."),
			@ApiResponse(responseCode = "403", description = "Device-control permission or a valid CSRF token is missing."),
			@ApiResponse(responseCode = "404", description = "The requested device does not exist.")
	})
	public ApiEnvelope<DeviceDTO> handleDeviceControl(
			@Parameter(description = "UUID of the device to control.", example = "11111111-1111-1111-1111-111111111111")
			@PathVariable String deviceId,
			@Valid @RequestBody DeviceControlRequestDTO controlRequest,
			@Parameter(hidden = true) Authentication authentication) {
		AuthenticatedUser currentUser = requireAuthenticatedUser(authentication);
		DeviceDTO device = smartHomeService.controlDevice(
				currentUser.userId().toString(),
				deviceId,
				controlRequest.command());
		return ApiEnvelope.ok(
				device,
				"Device turned " + device.status().toLowerCase(Locale.ROOT) + ".");
	}

	@PatchMapping("/members/{memberId}/permissions")
	@PreAuthorize("hasRole('HOMEOWNER')")
	@Operation(
			summary = "Update member permissions",
			description = "Allows a homeowner to change a family member's device-control and energy-view permissions.",
			security = {
					@SecurityRequirement(name = "sessionCookie"),
					@SecurityRequirement(name = "csrfToken")
			})
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Member permissions updated successfully."),
			@ApiResponse(responseCode = "400", description = "Permission values or member type are invalid."),
			@ApiResponse(responseCode = "401", description = "No authenticated session exists."),
			@ApiResponse(responseCode = "403", description = "Homeowner access or a valid CSRF token is missing."),
			@ApiResponse(responseCode = "404", description = "The requested member does not exist.")
	})
	public ApiEnvelope<UserDTO> handlePermissionUpdate(
			@Parameter(description = "UUID of the family member.", example = "22222222-2222-2222-2222-222222222222")
			@PathVariable String memberId,
			@Valid @RequestBody PermissionUpdateRequestDTO permissionRequest,
			@Parameter(hidden = true) Authentication authentication) {
		AuthenticatedUser currentUser = requireAuthenticatedUser(authentication);
		UserDTO member = smartHomeService.setMemberPermissions(
				currentUser.userId().toString(),
				memberId,
				permissionRequest.canControlDevices(),
				permissionRequest.canViewEnergy());
		return ApiEnvelope.ok(member, "Member permissions updated.");
	}

	private AuthenticatedUser requireAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new IllegalStateException("Authenticated user details are unavailable.");
		}
		return user;
	}
}
