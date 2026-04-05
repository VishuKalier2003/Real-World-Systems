package authentication.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import authentication.service.model.Registrations;
import authentication.service.repo.RegistrationRepo;

@Service
public class Storage {

    @Autowired private RegistrationRepo db;
    @Autowired private Encryption encryption;

    public Object[] register(String username, String email, String phone, String password) {
        if(db.findByUsername(username).isPresent())
            return new Object[]{false, "Username exists"};
        else if(db.findByPhone(phone).isPresent())
            return new Object[]{false, "Phone number exists"};
        else if(db.findByEmail(email).isPresent())
            return new Object[]{false, "Email already exists"};
        Registrations register = new Registrations();
        register.setUsername(username);
        register.setPhone(phone);
        register.setEmail(email);
        register.setPassword(encryption.encryptPassword(password));
        db.save(register);
        return new Object[]{true, "User registered successfully"};
    }
}
