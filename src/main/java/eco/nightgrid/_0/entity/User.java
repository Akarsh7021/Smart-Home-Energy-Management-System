package eco.nightgrid._0.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "user_id", nullable = false, updatable = false)
	private UUID userId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	@Column(name = "can_control_devices", nullable = false)
	private boolean canControlDevices;

	@Column(name = "can_view_energy", nullable = false)
	private boolean canViewEnergy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected User() {
	}

	public User(String name, String email, String passwordHash, UserRole role,
			boolean canControlDevices, boolean canViewEnergy) {
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.canControlDevices = canControlDevices;
		this.canViewEnergy = canViewEnergy;
	}

	public boolean hasControlPermission() {
		return role == UserRole.HOMEOWNER || canControlDevices;
	}

	public boolean hasEnergyPermission() {
		return role == UserRole.HOMEOWNER || canViewEnergy;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public boolean isCanControlDevices() {
		return canControlDevices;
	}

	public void setCanControlDevices(boolean canControlDevices) {
		this.canControlDevices = canControlDevices;
	}

	public boolean isCanViewEnergy() {
		return canViewEnergy;
	}

	public void setCanViewEnergy(boolean canViewEnergy) {
		this.canViewEnergy = canViewEnergy;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
