package authentication.service.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import authentication.service.model.Registrations;

public interface RegistrationRepo extends JpaRepository<Registrations, Long> {
    Optional<Registrations> findByPhone(String phone);

    Optional<Registrations> findByUsername(String username);

    Optional<Registrations> findByEmail(String email);
}
