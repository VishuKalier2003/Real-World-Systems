package authentication.two.service.limiter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RateLimiter {

    @Autowired private LimitCounter counter;
    @Autowired private LimiterBlockList blockList;

    public boolean requestReceived(String phone) {      // If true, allow request
        if(blockList.isBlocked(phone))
            return false;
        else if(counter.check(phone)) {
            blockList.block(phone);
            counter.emptyQueue(phone);
            return false;
        }
        else {
            counter.insert(phone);
            return true;
        }
    }
}
