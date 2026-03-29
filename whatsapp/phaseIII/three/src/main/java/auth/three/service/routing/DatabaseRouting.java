package auth.three.service.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import auth.three.model.Users;
import auth.three.repo.read.UsersReadRepo;
import auth.three.repo.write.UsersWriteRepo;

@Service
public class DatabaseRouting {
    @Autowired private UsersReadRepo readUser;
    @Autowired private UsersWriteRepo writeUser;
    private final Logger log = LoggerFactory.getLogger(DatabaseRouting.class);

    @Transactional(transactionManager="writeTransactionManager")
    public boolean save(Users user) {
        writeUser.save(user);
        log.info("{} saved to write DB",user.getName());
        return true;
    }

    @Transactional(readOnly = true, transactionManager = "readTransactionManager")
    public Users findByPhone(String phone) {
        return readUser.findByPhone(phone).orElseGet(
            () -> {
                log.warn("{} not found in read DB, checking in write DB ",phone);
                return fallbackToWrite(phone);
            }
        );
    }

    @Transactional(readOnly = true, transactionManager = "readTransactionManager")
    public Users findByName(String name) {
        return readUser.findByName(name).orElse(null);
    }

    @Transactional(readOnly = true, transactionManager = "writeTransactionManager")
    public Users fallbackToWrite(String name) {
        return writeUser.findByPhone(name).orElse(null);
    }
}
