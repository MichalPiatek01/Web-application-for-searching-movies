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
        Comment Comment = commentRepository.findByUser_UserIdAndMovie_MovieId(userId, movieId);
        commentRepository.delete(Comment);
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
    public Integer getMovieScore(Long movieId) {
        List<Comment> comments = commentRepository.findAllByMovie_MovieId(movieId);
        if (!comments.isEmpty()) {
            int score = 0;
            for (Comment comment : comments) {
                score = score + comment.getRating();
            }
            return score / comments.size();
        }
        return null;
    }
}
