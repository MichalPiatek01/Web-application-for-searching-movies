package webapp.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
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
import webapp.entities.*;
import webapp.mappers.MovieMapper;
import webapp.services.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.atmosphere.annotation.AnnotationUtil.logger;

@CssImport("./style.css")
@Route("")
@PermitAll
public class MainView extends VerticalLayout {
    private final MovieService movieService;
    private final transient AuthenticationContext authContext;
    private final MovieMapper movieMapper;
    private final WatchedService watchedService;
    private final UserService userService;
    private final CommentService commentService;
    TextField searchField = new TextField();
    Div infoDiv = new Div();
    Div titleDiv = new Div();
    Div yearDiv = new Div();
    Div genreDiv = new Div();
    Div moviesDiv = new Div();


    public MainView(MovieService movieService, AuthenticationContext authContext, MovieMapper movieMapper,
                    WatchedService watchedService, UserService userService, CommentService commentService) {
        getStyle().set("font-size", "20px");
        this.movieService = movieService;
        this.authContext = authContext;
        this.movieMapper = movieMapper;
        this.watchedService = watchedService;
        this.userService = userService;
        this.commentService = commentService;
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

    private Button getBackToMainViewButton() {
        Button backToMainButton = new Button("Main Page");
        backToMainButton.addClickListener(event -> {
            clearDivs();
            moviesDiv.add(getWatchedMoviesView());
        });
        return backToMainButton;
    }

    private void searchMovie(String searchString) {
        if (searchString == null || searchString.trim().isEmpty()) {
            return;
        }

        clearDivs();
        searchField.clear();
        titleDiv.getStyle().set("display", "block");
        yearDiv.getStyle().set("display", "block");
        infoDiv.getStyle().set("display", "block");
        genreDiv.getStyle().set("display", "block");

        try {
            Movie movie = movieService.SendRequest(searchString);
            if (!Objects.equals(movie.getPoster(), "N/A")) {
                Image posterImage = new Image(movie.getPoster(), "Poster");
                posterImage.setWidth("300px");
                posterImage.setHeight("500px");
                infoDiv.add(posterImage);
            } else {
                infoDiv.add("Poster not available.");
            }
            titleDiv.addClassName("movie-title");

            String trailerUrl = movieService.getTrailer(movie.getTitle());
            if (trailerUrl != null) {
                String videoId = trailerUrl.substring(trailerUrl.lastIndexOf("=") + 1);
                String iframe = "<iframe width='900' height='500' src='https://www.youtube.com/embed/"
                        + videoId + "' frameborder='0' allowfullscreen></iframe>";
                Html videoFrame = new Html(iframe);
                videoFrame.getElement().getStyle().set("margin-left", "50px");
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

            infoDiv.add(addBookmark(movie.getTitle()));
            titleDiv.add(movie.getTitle());
            if (!Objects.equals(movie.getPlot(), "N/A")) {
                genreDiv.add(movie.getPlot());
            } else {
                genreDiv.add("Plot not available.");
            }

            HorizontalLayout yearLayout = new HorizontalLayout();
            Span movieDetails = movieService.getMovieInfo(movie);
            HorizontalLayout ratingWrapper = new HorizontalLayout(addRating(movie));

            Div spacer = new Div();
            spacer.setWidth("950px");

            yearLayout.add(movieDetails, spacer, ratingWrapper);
            yearDiv.add(yearLayout);

            addRatingAndComments(movie.getTitle());
            addAllComments(movie.getTitle());
        } catch (MovieServiceImpl.MovieNotFoundException e) {
            infoDiv.add("Error: " + e.getMessage());
        }
    }

    private void addRatingAndComments(String title) {
        Div spacing = new Div();
        spacing.setHeight("20px");
        add(spacing);

        HorizontalLayout ratingLayout = new HorizontalLayout();
        ratingLayout.addClassName("rating-layout");

        Span ratingLabel = new Span("Rate this movie:");
        TextField ratingField = new TextField();
        ratingField.setPlaceholder("Enter rating (1-10)");

        TextField commentField = new TextField();
        commentField.setPlaceholder("Write your thoughts...");

        Button submitButton = new Button("Submit");

        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(userDetails -> {
            User user = userService.findByUsername(userDetails.getUsername());
            MovieDB movie = movieService.findByTitle(title);

            if (user != null && movie != null) {
                Comment existingComment = commentService.getComment(user.getUserId(), movie.getMovieId());

                if (existingComment != null) {
                    ratingLabel.setText("Edit your comment:");
                    submitButton.setText("Edit Comment");
                    submitButton.addClickListener(event -> {
                        try {
                            int ratingValue = Integer.parseInt(ratingField.getValue());
                            if (ratingValue < 1 || ratingValue > 10) {
                                logger.info("Invalid rating: {}. Must be between 1 and 10.", ratingValue);
                                return;
                            }

                            existingComment.setRating(ratingValue);
                            existingComment.setCommentText(commentField.getValue());
                            commentService.save(existingComment);

                            logger.info("User {} updated their comment on {}: Rating: {}, Comment: {}",
                                    user.getUsername(), title, ratingValue, existingComment.getCommentText());
                            searchMovie(title);
                        } catch (NumberFormatException e) {
                            logger.info("Invalid rating input: {}. Must be a number.", ratingField.getValue());
                        }
                    });

                } else {
                    submitButton.setText("Submit");
                    submitButton.addClickListener(event -> {
                        try {
                            int ratingValue = Integer.parseInt(ratingField.getValue());
                            if (ratingValue < 1 || ratingValue > 10) {
                                logger.info("Invalid rating: {}. Must be between 1 and 10.", ratingValue);
                                return;
                            }

                            Comment newComment = new Comment();
                            newComment.setUser(user);
                            newComment.setMovie(movie);
                            newComment.setRating(ratingValue);
                            newComment.setCommentText(commentField.getValue());

                            commentService.save(newComment);
                            logger.info("User {} rated {} with {} and commented: {}", user.getUsername(), title, ratingValue, newComment.getCommentText());
                            searchMovie(title);
                        } catch (NumberFormatException e) {
                            logger.info("Invalid rating input: {}. Must be a number.", ratingField.getValue());
                        }
                    });
                }
            }
        });

        ratingLayout.add(ratingLabel, ratingField, commentField, submitButton);
        ratingLayout.setAlignItems(Alignment.CENTER);
        genreDiv.add(spacing, ratingLayout);
    }


    private void addAllComments(String title) {
        Div spacing = new Div();
        spacing.setHeight("20px");
        add(spacing);

        VerticalLayout commentsLayout = new VerticalLayout();
        commentsLayout.addClassName("comments-section");

        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(userDetails -> {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username);
            MovieDB movie = movieService.findByTitle(title);

            List<Comment> allComments = commentService.getAllComments(movie.getMovieId());

            if (allComments.isEmpty()) {
                commentsLayout.add(new Span("No ratings yet."));
            } else {
                Comment userComment = allComments.stream()
                        .filter(comment -> comment.getUser().getUserId().equals(user.getUserId()))
                        .findFirst()
                        .orElse(null);

                allComments.remove(userComment);

                if (userComment != null) {
                    String userCommentText = "Your comment: " + userComment.getCommentText() +
                            " (Rating: " + userComment.getRating() + ")";
                    Span userCommentSpan = new Span(userCommentText);
                    userCommentSpan.addClassName("user-comment");
                    commentsLayout.add(userCommentSpan);
                }

                for (Comment comment : allComments) {
                    String commentText = comment.getUser().getUsername() + ": " + comment.getCommentText() +
                            " (Rating: " + comment.getRating() + ")";
                    Span commentSpan = new Span(commentText);
                    commentSpan.addClassName("comment-item");
                    commentsLayout.add(commentSpan);
                }
            }
        });

        genreDiv.add(spacing, commentsLayout);
    }

    private HorizontalLayout addRating(Movie movie) {
        HorizontalLayout scoreLayout = new HorizontalLayout();

        Span appScoreSpan = new Span();
        Span imdbRating = new Span();
        Span metaScore = new Span();
        if (!Objects.equals(movie.getImdbRating(), "N/A") ||
                !Objects.equals(movie.getMetascore(), "N/A")) {

            imdbRating.add("Imdb Rating: " + movie.getImdbRating() + "/10");
            metaScore.add("Metascore: " + movie.getMetascore() + "/100");
        } else {
            imdbRating.add("Imdb Rating not available.");
            metaScore.add("Metascore not available.");
        }

        Integer appScore = commentService.getMovieScore(movieService.findByTitle(movie.getTitle()).getMovieId());
        if (appScore == null) {
            appScoreSpan.setText("No website rating yet.");
        } else {
            appScoreSpan.setText("Website Rating: " + appScore + "/10");
        }

        scoreLayout.add(imdbRating, metaScore, appScoreSpan);

        return scoreLayout;
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
        clearDivs();
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

    private void clearDivs() {
        titleDiv.removeAll();
        yearDiv.removeAll();
        genreDiv.removeAll();
        infoDiv.removeAll();
        moviesDiv.removeAll();

        titleDiv.getStyle().set("display", "none");
        yearDiv.getStyle().set("display", "none");
        infoDiv.getStyle().set("display", "none");
        genreDiv.getStyle().set("display", "none");
    }
}