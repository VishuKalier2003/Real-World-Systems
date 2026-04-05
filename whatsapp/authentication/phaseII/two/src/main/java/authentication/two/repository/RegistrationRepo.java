package authentication.two.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import authentication.two.model.Registration;

public interface RegistrationRepo extends JpaRepository<Registration, Long> {
    
    Optional<Registration> findByUsername(String username);

    Optional<Registration> findByPhone(String phone);
}
