package message.phase1.service.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import message.phase1.api.dto.UserDto;
import message.phase1.exceptions.NoUserException;
import message.phase1.model.Users;
import message.phase1.repository.UsersRepository;

/** Class that interacts and acts as an adapter for <code>UsersRepository</code> with <code>Facade</code> */
@Service
public class UsersAdapter {
    
    @Autowired private UsersRepository usersRepository;

    public Long save(UserDto dto) {
        // Resolved the NPE with try-catch
        try {
            getUserIdByPhone(dto.getPhone());
        } catch (Exception e) {
            Users user = new Users();
            user.setPhone(dto.getPhone());
            user.setUsername(dto.getUsername());
            user.setStatus(dto.getStatus());
            usersRepository.save(user);
            return user.getUserId();
        }
        return null;
    }

    public Long getUserIdByPhone(String phone) {
        try {
        Users user = usersRepository.findByPhone(phone).orElse(null);
        if(user == null)
            throw new NoUserException("User not found with phone: " + phone);
        return user.getUserId();
        } catch (NoUserException e) {
            return Long.MIN_VALUE;
        }
    }

    public Users getUserById(Long userId) {
        return usersRepository.findById(userId).orElse(null);
    }
}
