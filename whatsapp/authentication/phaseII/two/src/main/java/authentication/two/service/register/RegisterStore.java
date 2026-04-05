package authentication.two.service.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import authentication.two.dto.Register;
import authentication.two.model.Registration;
import authentication.two.repository.RegistrationRepo;

@Service
public class RegisterStore {
    
    @Autowired private RegistrationRepo repo;
    @Autowired @Qualifier("encryption") PasswordEncoder encoder;

    public boolean checkifExists(Register registration) {   // If true, user exists
        return repo.findByUsername(registration.getUsername()).isPresent() || repo.findByPhone(registration.getPhone()).isPresent();
    }

    public boolean register(Register register) {
        try {
            register.setPassword(encoder.encode(register.getPassword()));   // Encode password before saving
            // Create new registration object
            Registration registration = new Registration();
            registration.setUsername(register.getUsername());
            registration.setEmail(register.getEmail());
            registration.setPassword(register.getPassword());
            registration.setPhone(register.getPhone());
            repo.save(registration);        // save in repo
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
