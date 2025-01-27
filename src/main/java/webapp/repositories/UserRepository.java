package webapp.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import webapp.entities.User;

public interface UserRepository extends CrudRepository<User, Integer> {

    @Query("SELECT u FROM User u " +
            "WHERE u.username = ?1")
    User findByUserName(String username);
}
