package webapp.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
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
import webapp.entities.MovieDB;
import webapp.entities.User;
import webapp.services.UserService;
import webapp.services.WatchedService;

import java.util.List;
import java.util.Objects;

@CssImport("./style.css")
@Route("")
@PermitAll
public class MainView extends VerticalLayout {
    private final transient AuthenticationContext authContext;
    private final WatchedService watchedService;
    private final UserService userService;
    TextField searchField = new TextField();
    Div moviesDiv = new Div();


    public MainView(AuthenticationContext authContext, WatchedService watchedService, UserService userService) {
        getStyle().set("font-size", "20px");
        this.authContext = authContext;
        this.watchedService = watchedService;
        this.userService = userService;
        add(getSearchbar());
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
                            return new HorizontalLayout(logout);
                        }).orElseGet(HorizontalLayout::new);

        HorizontalLayout searchLayout = new HorizontalLayout(getBackToMainViewButton(), searchField, searchButton);
        HorizontalLayout searchbar = new HorizontalLayout(searchLayout, header);
        searchbar.setClassName("searchbar");

        searchbar.setWidthFull();
        searchbar.addClassName("custom-layout");
        searchbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        return searchbar;
    }

    private void searchMovie(String movieTitle) {
        if (!movieTitle.trim().isEmpty()) {
            UI.getCurrent().navigate(MovieView.class, movieTitle);
        }
    }

    private Button getBackToMainViewButton() {
        Button backToMainButton = new Button("Main Page");
        backToMainButton.addClickListener(event -> {
            UI.getCurrent().navigate(MainView.class);
        });
        return backToMainButton;
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

                        if (Objects.equals(movie.getPoster(), "N/A") || movie.getPoster() == null || movie.getPoster().isEmpty()) {
                            Span posterText = new Span("Poster not available");
                            posterText.addClassName("poster-placeholder");
                            posterText.addClickListener(event -> searchMovie(movie.getTitle()));
                            movieLayout.add(posterText);
                        } else {
                            Image poster = new Image(movie.getPoster(), "Poster of " + movie.getTitle());
                            poster.addClassName("movie-poster");
                            poster.setWidth("300px");
                            poster.setHeight("400px");
                            poster.addClickListener(event -> searchMovie(movie.getTitle()));
                            movieLayout.add(poster);
                        }

                        Span title = new Span(movie.getTitle());
                        title.addClassName("movie-title");
                        title.addClickListener(event -> searchMovie(movie.getTitle()));

                        movieLayout.add(title);
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
        VerticalLayout watchedMoviesLayout2 = new VerticalLayout();
        Span watchlist = new Span("Watchlist:");
        watchlist.addClassName("watchlist");
        watchedMoviesLayout2.add(watchlist, watchedMoviesLayout);
        watchedMoviesLayout.addClassName("watched-movies-layout2");
        return watchedMoviesLayout2;
    }

//    private void clearDivs() {
//        titleDiv.removeAll();
//        yearDiv.removeAll();
//        genreDiv.removeAll();
//        infoDiv.removeAll();
//        moviesDiv.removeAll();
//
//        titleDiv.getStyle().set("display", "none");
//        yearDiv.getStyle().set("display", "none");
//        infoDiv.getStyle().set("display", "none");
//        genreDiv.getStyle().set("display", "none");
//    }
}