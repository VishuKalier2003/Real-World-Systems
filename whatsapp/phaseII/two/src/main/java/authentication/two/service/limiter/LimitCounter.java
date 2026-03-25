package authentication.two.service.limiter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Service;

@Service
public class LimitCounter {
    private final Map<String, ConcurrentLinkedDeque<Instant>> counter = new ConcurrentHashMap<>();

    public void insert(String phone) {
        counter.putIfAbsent(phone, new ConcurrentLinkedDeque<>());
        counter.get(phone).addLast(Instant.now());
    }

    public boolean check(String phone) {
        ConcurrentLinkedDeque<Instant> queue = counter.get(phone);
        if(queue == null)
            return false;
        while(!queue.isEmpty() && queue.getFirst().isBefore(Instant.now().minusSeconds(30))) {
            queue.pollFirst();
        }
        return queue.size() >= 10;
    }

    public void emptyQueue(String phone) {
        counter.get(phone).clear();
    }
}
