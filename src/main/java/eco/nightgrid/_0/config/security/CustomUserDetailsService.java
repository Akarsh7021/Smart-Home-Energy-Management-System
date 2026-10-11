package eco.nightgrid._0.config.security;

import eco.nightgrid._0.dao.UserDAO;
import eco.nightgrid._0.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserDAO userDAO;

	public CustomUserDetailsService(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userDAO.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User account was not found."));

		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

		if (user.hasControlPermission()) {
			authorities.add(new SimpleGrantedAuthority("CONTROL_DEVICES"));
		}
		if (user.hasEnergyPermission()) {
			authorities.add(new SimpleGrantedAuthority("VIEW_ENERGY"));
		}

		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPasswordHash())
				.authorities(authorities)
				.build();
	}
}
