package webapp.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import webapp.entities.User;


public interface UserService extends UserDetailsService {

    User findByUsername(String username);

    void save(User user);
}