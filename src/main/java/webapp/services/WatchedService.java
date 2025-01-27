package webapp.services;

import webapp.entities.MovieDB;
import webapp.entities.Watched;

import java.util.List;

public interface WatchedService {

    void save(Watched watched);

    void delete(Watched watched);

    Watched getWatched(Long userId, Long movieId);

    boolean isInWatched(Long userId, Long movieId);

    List<MovieDB> getAllWatchedByUserId(Long userId);
}
