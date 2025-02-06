package webapp.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import webapp.entities.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {

    Comment findByUser_UserIdAndMovie_MovieId(Long userUserId, Long movieMovieId);

    List<Comment> findAllByMovie_MovieId(Long movieId);
}
