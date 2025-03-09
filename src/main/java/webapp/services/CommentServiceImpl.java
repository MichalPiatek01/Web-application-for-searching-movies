package webapp.services;

import org.springframework.stereotype.Service;
import webapp.entities.Comment;
import webapp.repositories.CommentRepository;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    @Override
    public void delete(Long userId, Long movieId) {
        Comment comment = commentRepository.findByUser_UserIdAndMovie_MovieId(userId, movieId);
        if (comment != null) {
            commentRepository.delete(comment);
        }
    }

    @Override
    public Comment getComment(Long userId, Long movieId) {
        return commentRepository.findByUser_UserIdAndMovie_MovieId(userId, movieId);
    }

    @Override
    public List<Comment> getAllComments(Long movieId) {
        return commentRepository.findAllByMovie_MovieId(movieId);
    }

    @Override
    public Float getMovieScore(Long movieId) {
        List<Comment> comments = commentRepository.findAllByMovie_MovieId(movieId);
        return comments.isEmpty() ? null
                : (float) comments.stream()
                .mapToInt(Comment::getRating)
                .average()
                .orElse(0);
    }

}
