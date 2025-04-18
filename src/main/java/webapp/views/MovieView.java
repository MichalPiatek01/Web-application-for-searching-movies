package webapp.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;
import webapp.entities.*;
import webapp.services.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.atmosphere.annotation.AnnotationUtil.logger;

@CssImport("./style.css")
@Route("/movie")
@PermitAll
public class MovieView extends VerticalLayout implements HasUrlParameter<String> {
    private final MovieService movieService;
    private final transient AuthenticationContext authContext;
    private final WatchedService watchedService;
    private final UserService userService;
    private final CommentService commentService;
    TextField searchField = new TextField();
    Div infoDiv = new Div();
    Div titleDiv = new Div();
    Div infoAndRatingsDiv = new Div();
    Div genreDiv = new Div();

    public MovieView(MovieService movieService, AuthenticationContext authContext, WatchedService watchedService,
                     UserService userService, CommentService commentService) {
        getStyle().set("font-size", "20px");
        this.movieService = movieService;
        this.authContext = authContext;
        this.watchedService = watchedService;
        this.userService = userService;
        this.commentService = commentService;
        add(getSearchbar(), titleDiv, infoAndRatingsDiv, infoDiv, genreDiv);
    }

    public HorizontalLayout getSearchbar() {
        searchField.setPlaceholder("Search by movie name");
        searchField.setClearButtonVisible(true);

        Button searchButton = new Button("Search");
        searchButton.addClickListener(event -> UI.getCurrent().navigate(MovieView.class,
                searchField.getValue()));
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
            UI.getCurrent().navigate("");
        });
        return backToMainButton;
    }

    private void searchMovie(String searchString) {
        if (searchString == null || searchString.trim().isEmpty()) {
            return;
        }

        clearDivs();
        searchField.clear();
        try {
            Movie movie = movieService.SendRequest(searchString);
            if (!Objects.equals(movie.getPoster(), "N/A")) {
                Image posterImage = new Image(movie.getPoster(), "Poster");
                posterImage.setWidth("350px");
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
                videoFrame.getElement().getStyle().set("margin-left", "175px");
                infoDiv.add(videoFrame);
            } else {
                infoDiv.add("Trailer not found.");
            }

            MovieDB movieDB = movieService.mapMovieToDB(movie);
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
            infoAndRatingsDiv.add(yearLayout);

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
                            logger.info("User {} rated {} with {} and commented: {}", user.getUsername(),
                                    title, ratingValue, newComment.getCommentText());
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
                    Span userCommentSpan = new Span("Your comment: " + userComment.getCommentText() +
                            " (Rating: " + userComment.getRating() + ")");
                    userCommentSpan.addClassName("user-comment");

                    Button deleteButton = new Button("Delete Comment", event -> {
                        commentService.delete(user.getUserId(), movie.getMovieId());
                        logger.info("User {} deleted their comment on {}", user.getUsername(), title);
                        searchMovie(title);
                    });
                    deleteButton.addClassName("delete-comment-button");

                    HorizontalLayout userCommentLayout = new HorizontalLayout(userCommentSpan, deleteButton);
                    userCommentLayout.setAlignItems(Alignment.CENTER);

                    commentsLayout.add(userCommentLayout);
                }

                for (Comment comment : allComments) {
                    HorizontalLayout commentLayout = new HorizontalLayout();
                    commentLayout.setAlignItems(Alignment.CENTER);

                    Span commentSpan = new Span(comment.getUser().getUsername() + ": " + comment.getCommentText() +
                            " (Rating: " + comment.getRating() + ")");
                    commentSpan.addClassName("comment-item");

                    commentLayout.add(commentSpan);

                    if (user.getRole().equals("ROLE_ADMIN")) {
                        Button adminDeleteButton = new Button("Delete", event -> {
                            commentService.delete(comment.getUser().getUserId(), movie.getMovieId());
                            logger.info("Admin {} deleted {}'s comment on {}", user.getUsername(),
                                    comment.getUser().getUsername(), title);
                            searchMovie(title);
                        });
                        adminDeleteButton.addClassName("admin-delete-button");
                        adminDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

                        commentLayout.add(adminDeleteButton);
                    }

                    commentsLayout.add(commentLayout);
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

        Float appScore = commentService.getMovieScore(movieService.findByTitle(movie.getTitle()).getMovieId());
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

    private void clearDivs() {
        titleDiv.removeAll();
        infoAndRatingsDiv.removeAll();
        genreDiv.removeAll();
        infoDiv.removeAll();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String movieTitle) {
        if (movieTitle != null && !movieTitle.isEmpty()) {
            searchMovie(movieTitle);
        } else {
            clearDivs();
            add(new Paragraph("Movie title is not provided."));
        }
    }
}