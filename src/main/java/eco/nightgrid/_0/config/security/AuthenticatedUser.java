package eco.nightgrid._0.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(
		UUID userId,
		String username,
		String password,
		List<GrantedAuthority> authorities) implements UserDetails {

	@Serial
	private static final long serialVersionUID = 1L;

	public AuthenticatedUser {
		authorities = List.copyOf(authorities);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}
}
