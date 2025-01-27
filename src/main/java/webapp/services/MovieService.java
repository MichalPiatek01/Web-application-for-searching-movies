package webapp.services;

import webapp.entities.Movie;
import webapp.entities.MovieDB;

public interface MovieService {

    Movie SendRequest(String title);

    String getTrailer(String title);

    MovieDB findByTitle(String title);

    boolean doesMovieExist(String title);

    void save(MovieDB movieDB);
}
