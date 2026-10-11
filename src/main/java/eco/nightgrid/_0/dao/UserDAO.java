package eco.nightgrid._0.dao;

import eco.nightgrid._0.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDAO extends JpaRepository<User, UUID> {

	Optional<User> findByEmail(String email);

	@Query("""
			select user
			from User user
			where user.role = eco.nightgrid._0.entity.UserRole.FAMILY_MEMBER
			order by user.name
			""")
	List<User> findAllMembers();
}
