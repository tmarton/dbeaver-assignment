package cz.tmarton.dbeavermvc;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
}
