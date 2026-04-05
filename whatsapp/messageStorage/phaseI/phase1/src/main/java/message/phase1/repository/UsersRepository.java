package message.phase1.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import message.phase1.model.Users;

/** Interface to define Field Navigation for <code>Users</code> entity */
public interface UsersRepository extends JpaRepository<Users, Long> {
    public Optional<Users> findByPhone(String phone);
}
