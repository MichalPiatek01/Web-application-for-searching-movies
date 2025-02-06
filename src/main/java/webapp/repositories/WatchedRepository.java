package webapp.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import webapp.entities.Watched;

import java.util.List;

@Repository
public interface WatchedRepository extends CrudRepository<Watched, Integer> {

    Watched findByUser_UserIdAndMovie_MovieId(Long userId, Long movieId);

    boolean existsByUser_UserIdAndMovie_MovieId(Long userId, Long movieId);

    List<Watched> findAllByUser_UserId(Long userId);
}
