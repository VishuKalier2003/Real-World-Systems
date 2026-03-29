package auth.three.repo.write;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import auth.three.model.Users;

public interface UsersWriteRepo extends JpaRepository<Users, Long> {
    Optional<Users> findByName(String name);
    Optional<Users> findByPhone(String phone);
}
