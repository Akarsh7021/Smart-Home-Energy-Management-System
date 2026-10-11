package eco.nightgrid._0.dto;

public record UserDTO(
		String userId,
		String name,
		String email,
		String role,
		boolean canControlDevices,
		boolean canViewEnergy) {
}
