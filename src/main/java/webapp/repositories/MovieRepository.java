package webapp.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import webapp.entities.MovieDB;

@Repository
public interface MovieRepository extends CrudRepository<MovieDB, Integer> {

    @Query("SELECT m FROM MovieDB m " +
            "WHERE m.title = ?1")
    MovieDB findByTitle(String title);

    boolean existsByTitle(String title);
}