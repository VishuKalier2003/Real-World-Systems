package authentication.two.service.limiter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LimiterBlockList {
    private final Map<String, Instant> blocklist = new ConcurrentHashMap<>();

    public void block(String phone) {
        blocklist.put(phone, Instant.now().plusSeconds(120));
    }

    public boolean isBlocked(String phone) {
        Instant blocked = blocklist.get(phone);
        if(blocked == null)
            return false;
        return blocked.isAfter(Instant.now());
    }

    public boolean ifExists(String phone) {
        return blocklist.containsKey(phone);
    }

    public void unblock(String phone) {
        blocklist.remove(phone);
    }
}
