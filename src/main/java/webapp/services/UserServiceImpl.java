package webapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import webapp.entities.User;
import webapp.repositories.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public void save(User user) {
        try {
            User users = new User();

            users.setUsername(user.getUsername());
            users.setPassword(passwordEncoder.encode(user.getPassword()));
            users.setEmail(user.getEmail());
            users.setRole("ROLE_USER");

            userRepository.save(users);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("narusza ograniczenie unikalności") && e.getMessage().contains("(email)")) {
                throw new DuplicateEmailException("Email '" + user.getEmail() + "' is already in use.");
            } else if (e.getMessage().contains("narusza ograniczenie unikalności") && e.getMessage().contains("(username)")) {
                throw new DuplicateUsernameException("Username '" + user.getUsername() + "' is already taken.");
            }
            throw e;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User users = userRepository.findByUserName(username);

        if (users == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(users.getRole()));

        return new org.springframework.security.core.userdetails.User(users.getUsername(), users.getPassword(), authorities);
    }

    public static class DuplicateUsernameException extends RuntimeException {
        public DuplicateUsernameException(String message) {
            super(message);
        }
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }
}
