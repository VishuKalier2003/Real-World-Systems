package four.auth.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import four.auth.model.Users;

public interface UserAuthRepo extends JpaRepository<Users, Long> {
    Optional<Users> findByPhone(String phone);
    Optional<Users> findByName(String name);
}
