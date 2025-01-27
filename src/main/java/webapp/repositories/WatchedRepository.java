package webapp.repositories;

import org.springframework.data.repository.CrudRepository;
import webapp.entities.Watched;

import java.util.List;

public interface WatchedRepository extends CrudRepository<Watched, Integer> {

    Watched findByUser_UserIdAndMovie_MovieId(Long userId, Long movieId);

    boolean existsByUser_UserIdAndMovie_MovieId(Long userId, Long movieId);

    List<Watched> findAllByUser_UserId(Long userId);
}
