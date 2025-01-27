package webapp.services;

import org.springframework.stereotype.Service;
import webapp.entities.MovieDB;
import webapp.entities.Watched;
import webapp.repositories.WatchedRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.atmosphere.annotation.AnnotationUtil.logger;

@Service
public class WatchedServiceImpl implements WatchedService {
    private final WatchedRepository watchedRepo;

    public WatchedServiceImpl(WatchedRepository watchedRepo) {
        this.watchedRepo = watchedRepo;
    }

    @Override
    public void save(Watched watched) {
        watchedRepo.save(watched);
        logger.info("Watched movie saved to database");
    }

    @Override
    public void delete(Watched watched) {
        watchedRepo.delete(watched);
        logger.info("Watched movie deleted from database");
    }

    @Override
    public Watched getWatched(Long userId, Long movieId) {
        logger.info("Watched movie saved to database");
        return watchedRepo.findByUser_UserIdAndMovie_MovieId(userId, movieId);
    }

    @Override
    public boolean isInWatched(Long userId, Long movieId) {
        return watchedRepo.existsByUser_UserIdAndMovie_MovieId(userId, movieId);
    }

    @Override
    public List<MovieDB> getAllWatchedByUserId(Long userId) {
        return watchedRepo.findAllByUser_UserId(userId).stream()
                .map(Watched::getMovie)
                .collect(Collectors.toList());
    }
}
