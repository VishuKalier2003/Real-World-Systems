package auth.three.repo.read;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import auth.three.model.Users;

public interface UsersReadRepo extends JpaRepository<Users, Long> {
    Optional<Users> findByPhone(String phone);
    Optional<Users> findByName(String name);
}
