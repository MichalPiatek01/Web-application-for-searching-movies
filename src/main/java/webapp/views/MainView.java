package webapp.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;
import webapp.entities.Movie;
import webapp.entities.MovieDB;
import webapp.entities.User;
import webapp.entities.Watched;
import webapp.mappers.MovieMapper;
import webapp.services.MovieService;
import webapp.services.UserService;
import webapp.services.WatchedService;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.atmosphere.annotation.AnnotationUtil.logger;


@Route("home")
@PermitAll
public class MainView extends VerticalLayout {
    private final MovieService movieService;
    private final transient AuthenticationContext authContext;
    private final MovieMapper movieMapper;
    private final WatchedService watchedService;
    private final UserService userService;
    TextField searchField = new TextField();
    Div infoDiv = new Div();
    Div titleDiv = new Div();
    Div yearDiv = new Div();
    Div genreDiv = new Div();
    Div moviesDiv = new Div();


    public MainView(MovieService movieService, AuthenticationContext authContext, MovieMapper movieMapper, WatchedService watchedService, UserService userService) {
        this.movieService = movieService;
        this.authContext = authContext;
        this.movieMapper = movieMapper;
        this.watchedService = watchedService;
        this.userService = userService;
        add(getSearchbar(), titleDiv, yearDiv, infoDiv, genreDiv);
        moviesDiv.add(getWatchedMoviesView());
        add(moviesDiv);
    }

    public HorizontalLayout getSearchbar() {
        searchField.setPlaceholder("Search by movie name");
        searchField.setClearButtonVisible(true);

        Button searchButton = new Button("Search");
        searchButton.addClickListener(event -> searchMovie(searchField.getValue()));
        searchButton.addClickShortcut(Key.ENTER);
        HorizontalLayout
                header =
                authContext.getAuthenticatedUser(UserDetails.class)
                        .map(user -> {
                            Button logout = new Button("Logout", click ->
                                    this.authContext.logout());
                            Span loggedUser = new Span("Welcome " + user.getUsername());
                            return new HorizontalLayout(loggedUser, logout);
                        }).orElseGet(HorizontalLayout::new);

        HorizontalLayout searchLayout = new HorizontalLayout(getBackToMainViewButton(), searchField, searchButton);
        HorizontalLayout searchbar = new HorizontalLayout(searchLayout, header);

        searchbar.setWidthFull();
        searchbar.addClassName("custom-layout");
        searchbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        return searchbar;
    }

    private Button getBackToMainViewButton() {
        Button backToMainButton = new Button("Main Page");
        backToMainButton.addClickListener(event -> {
            clearDivs();
            moviesDiv.add(getWatchedMoviesView());
        });
        return backToMainButton;
    }

    private void searchMovie(String searchString) {
        clearDivs();
        Movie movie = movieService.SendRequest(searchString);
        Image posterImage = new Image(movie.getPoster(), "Poster");
        posterImage.setWidth("300px");
        infoDiv.add(posterImage);
        String trailerUrl = movieService.getTrailer(searchString);
        if (trailerUrl != null) {
            String videoId = trailerUrl.substring(trailerUrl.lastIndexOf("=") + 1);

            String iframe = "<iframe width='800' height='450' src='https://www.youtube.com/embed/"
                    + videoId
                    + "' frameborder='0' allowfullscreen></iframe>";
            Html videoFrame = new Html(iframe);
            infoDiv.add(videoFrame);
        } else {
            infoDiv.add("Trailer not found.");
        }
        MovieDB movieDB = movieMapper.mapMovieDB(movie);
        if (movieDB.getTitle() != null && !movieDB.getTitle().isBlank() &&
                !movieService.doesMovieExist(movieDB.getTitle())) {
            movieService.save(movieDB);
            logger.info("Saved movie: {}", movieDB.getTitle());
        } else {
            logger.info("Movie already in database: {}", movieDB.getTitle());
        }
        infoDiv.add(addBookmark(movieDB.getTitle()));
        genreDiv.add(movie.getPlot());
        titleDiv.add(movie.getTitleFromResponse());
        yearDiv.add(movie.getYear() + " " + movie.getRated() + " " + movie.getRuntime() + " " + movie.getGenre());
    }

    private Div addBookmark(String title) {
        Image emptyImage = new Image("images/pusty.png", "Bookmark");
        emptyImage.setWidth("50px");
        emptyImage.setHeight("50px");
        emptyImage.getElement().getStyle().set("display", "block");
        emptyImage.getElement().getStyle().set("margin", "auto");
        Div bookmarkDiv = new Div(emptyImage);
        bookmarkDiv.getElement().getStyle().set("position", "absolute");
        bookmarkDiv.getElement().getStyle().set("top", "75px");
        bookmarkDiv.getElement().getStyle().set("right", "50px");
        String fullImageSrc = "images/pełny.png";
        String emptyImageSrc = "images/pusty.png";
        authContext.getAuthenticatedUser(UserDetails.class).map(userDetails -> {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username);
            Watched watched = new Watched();
            watched.setUser(user);
            watched.setMovie(movieService.findByTitle(title));
            boolean isAlreadyBookmarked = watchedService.isInWatched(user.getUserId(), watched.getMovie().getMovieId());
            if (isAlreadyBookmarked) {
                emptyImage.setSrc(fullImageSrc);
                emptyImage.setWidth("75px");
                emptyImage.setHeight("75px");
            }
            AtomicBoolean isBookmarked = new AtomicBoolean(isAlreadyBookmarked);
            emptyImage.addClickListener(event -> {
                if (isBookmarked.get()) {
                    watchedService.delete(watchedService.getWatched(user.getUserId(), watched.getMovie().getMovieId()));
                    emptyImage.setSrc(emptyImageSrc);
                    emptyImage.setWidth("50px");
                    emptyImage.setHeight("50px");
                } else {
                    watchedService.save(watched);
                    emptyImage.setSrc(fullImageSrc);
                    emptyImage.setWidth("75px");
                    emptyImage.setHeight("75px");
                }
                isBookmarked.set(!isBookmarked.get());
            });
            return null;
        });

        return bookmarkDiv;
    }

    private VerticalLayout getWatchedMoviesView() {
        VerticalLayout watchedMoviesLayout = new VerticalLayout();
        watchedMoviesLayout.addClassName("watched-movies-layout");
        authContext.getAuthenticatedUser(UserDetails.class).map(userDetails -> {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username);
            if (user != null) {
                List<MovieDB> watchedMovies = watchedService.getAllWatchedByUserId(user.getUserId());
                if (watchedMovies.isEmpty()) {
                    watchedMoviesLayout.add(new Span("No watched movies found."));
                } else {
                    HorizontalLayout movieRow = new HorizontalLayout();
                    int movieCount = 0;
                    for (MovieDB movie : watchedMovies) {
                        VerticalLayout movieLayout = new VerticalLayout();
                        movieLayout.addClassName("movie-item");
                        Image poster = new Image(movie.getPoster(), "Poster of " + movie.getTitle());
                        poster.addClassName("movie-poster");
                        poster.addClickListener(event -> searchMovie(movie.getTitle()));
                        Span title = new Span(movie.getTitle());
                        title.addClassName("movie-title");
                        title.addClickListener(event -> searchMovie(movie.getTitle()));
                        movieLayout.add(poster, title);
                        movieLayout.setAlignItems(Alignment.CENTER);
                        movieRow.add(movieLayout);
                        movieCount++;
                        if (movieCount == 5) {
                            watchedMoviesLayout.add(movieRow);
                            movieRow = new HorizontalLayout();
                            movieCount = 0;
                        }
                    }
                    if (movieCount > 0) {
                        watchedMoviesLayout.add(movieRow);
                    }
                }
            } else {
                watchedMoviesLayout.add(new Span("No user found in the database."));
            }
            return null;
        });

        return watchedMoviesLayout;
    }

    private void clearDivs() {
        titleDiv.removeAll();
        yearDiv.removeAll();
        genreDiv.removeAll();
        infoDiv.removeAll();
        moviesDiv.removeAll();
    }
}