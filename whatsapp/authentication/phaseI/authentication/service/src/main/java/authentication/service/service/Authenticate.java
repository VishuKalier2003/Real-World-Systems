package authentication.service.service;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import authentication.service.database.SessionStore;
import authentication.service.repo.RegistrationRepo;

@Service
public class Authenticate {
    
    @Autowired private RegistrationRepo db;
    @Autowired private SessionStore store;
    @Autowired private Encryption encryption;

    private final ReentrantLock lock = new ReentrantLock();
    private static final String ALPHABETS = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
    private final Random random = new Random();

    public boolean authenticate(String username) {
        try {
            lock.lock();
            return db.findByUsername(username).isPresent();
        } catch (Exception e) {
            return false;
        } finally {
            lock.unlock();
        }
    }

    // Generate a random token
    private String generateToken() {
        StringBuilder sb = new StringBuilder();
        int n = ALPHABETS.length();
        for(int i = 0; i < 50; i++)
            sb.append(ALPHABETS.charAt(random.nextInt(n)));
        return sb.toString();
    }

    public String provideToken(String username) {
        final String token = generateToken();
        store.addSession(username, encryption.encryptPassword(token));
        return token;
    }

    public boolean validate(String username, String token) {
        if(!store.activeSession(username))
            return false;
        String storedHash = store.getSessionToken(username);
        return encryption.matches(token, storedHash);
    }

    public boolean removeSession(String username) {
        store.closeSession(username);
        return true;
    }
}
