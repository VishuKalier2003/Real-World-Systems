package authentication.service.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SessionStore {
    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void addSession(String username, String token) {
        store.put(username, token);
    }

    public boolean activeSession(String username) {return store.containsKey(username);}

    public String getSessionToken(String username) {return store.get(username);}

    public void closeSession(String username) {store.remove(username);}
}
