package webapp.services;

import com.vaadin.flow.component.html.Span;
import webapp.entities.Movie;
import webapp.entities.MovieDB;

public interface MovieService {

    Movie SendRequest(String title) throws MovieServiceImpl.MovieNotFoundException;

    String getTrailer(String title);

    MovieDB findByTitle(String title);

    boolean doesMovieExist(String title);

    void save(MovieDB movieDB);

    Span getMovieInfo(Movie movie);
}
