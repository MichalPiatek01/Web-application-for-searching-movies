package webapp.services;

import webapp.entities.Comment;

import java.util.List;

public interface CommentService {

    void save(Comment comment);

    void delete(Long userId, Long movieId);

    Comment getComment(Long userId, Long movieId);

    List<Comment> getAllComments(Long movieId);

    Float getMovieScore(Long movieId);
}
