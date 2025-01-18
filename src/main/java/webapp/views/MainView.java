package webapp.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;
import webapp.entities.Movie;
import webapp.services.MovieService;

import java.util.concurrent.atomic.AtomicBoolean;


@Route("home")
@PermitAll
public class MainView extends VerticalLayout {
    private final MovieService movieService;
    private final transient AuthenticationContext authContext;
    TextField searchField = new TextField();
    Div infoDiv = new Div();
    Div titleDiv = new Div();
    Div yearDiv = new Div();
    Div genreDiv = new Div();
    TextField usernameField = new TextField("Username");
    TextField emailField = new TextField("Email");
    PasswordField passwordField = new PasswordField("Password");


    public MainView(MovieService movieService, AuthenticationContext authContext) {
        this.movieService = movieService;
        this.authContext = authContext;
        add(getSearchbar(), titleDiv, yearDiv, infoDiv, genreDiv, getUserSaveSection());
    }

    public HorizontalLayout getSearchbar() {
        searchField.setPlaceholder("Search by movie name");
        searchField.setClearButtonVisible(true);

        Button searchButton = new Button("Search");
        searchButton.addClickListener(event -> searchMovie());
        searchButton.addClickShortcut(Key.ENTER);
//
//        Button logout = new Button("Logout", click ->
//                securityService.logout());
////        if (securityService.getAuthenticatedUser() != null) {
////            Button logout = new Button("Logout", click ->
////                    securityService.logout());
////        }
        H1 logo = new H1("Vaadin CRM");
        logo.addClassName("logo");
        HorizontalLayout
                header =
                authContext.getAuthenticatedUser(UserDetails.class)
                        .map(user -> {
                            Button logout = new Button("Logout", click ->
                                    this.authContext.logout());
                            Span loggedUser = new Span("Welcome " + user.getUsername());
                            return new HorizontalLayout(logo, loggedUser, logout);
                        }).orElseGet(() -> new HorizontalLayout(logo));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, searchButton);
        HorizontalLayout searchbar = new HorizontalLayout(searchLayout, header);

        searchbar.setWidthFull();
        searchbar.addClassName("custom-layout");
        searchbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        return searchbar;
    }

//    private Button getBackToMainViewButton() {
//        Button backToMainButton = new Button("Main Page");
//        backToMainButton.addClickListener(event -> {
//            // Retrieve the authenticated user's details
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication != null && authentication.isAuthenticated()) {
//                Object principal = authentication.getPrincipal();
//                if (principal instanceof UserDetails) {
//                    UserDetails userDetails = (UserDetails) principal;
//                    System.out.println("Logged-in user: " + userDetails.getUsername());
//                    System.out.println("Authorities: " + userDetails.getAuthorities());
//                } else {
//                    System.out.println("Logged-in user: " + principal.toString());
//                }
//            } else {
//                System.out.println("No authenticated user found.");
//            }
//
//            // Navigate to MainView
//            UI.getCurrent().navigate(MainView.class);
//        });
//        return backToMainButton;
//    }

    private void searchMovie() {
        // Clear previous content
        titleDiv.removeAll();
        yearDiv.removeAll();
        genreDiv.removeAll();
        infoDiv.removeAll();

        // Get the search string from the input field
        String searchString = searchField.getValue();

        // Fetch movie details
        Movie movie = movieService.SendRequest(searchString);

        // Display the movie poster
        Image posterImage = new Image(movie.getPoster(), "Poster");
        posterImage.setWidth("300px");
        infoDiv.add(posterImage);

        // Fetch the trailer URL
        String trailerUrl = movieService.getTrailer(searchString);

        if (trailerUrl != null) {
            // Extract the video ID from the trailer URL
            String videoId = trailerUrl.substring(trailerUrl.lastIndexOf("=") + 1);

            // Create an iframe for the YouTube video
            String iframe = "<iframe width='800' height='450' src='https://www.youtube.com/embed/"
                    + videoId
                    + "' frameborder='0' allowfullscreen></iframe>";

            // Embed the iframe into the UI
            Html videoFrame = new Html(iframe);
            infoDiv.add(videoFrame);
        } else {
            // Display a message if the trailer is not found
            infoDiv.add("Trailer not found.");
        }
        infoDiv.add(addBookmark());
        genreDiv.add(movie.getPlot());
        titleDiv.add(movie.getTitleFromResponse());
        yearDiv.add(movie.getYear() + " " + movie.getRated() + " " + movie.getRuntime() + " " + movie.getGenre());
    }

    private HorizontalLayout getUserSaveSection() {
        usernameField.setPlaceholder("Enter username");
        emailField.setPlaceholder("Enter email");
        passwordField.setPlaceholder("Enter password");

//        Button saveUserButton = new Button("Save User");
//        saveUserButton.addClickListener(event -> saveUser());

        HorizontalLayout userSaveLayout = new HorizontalLayout(usernameField, emailField, passwordField);
        return userSaveLayout;
    }

//    private void saveUser() {
//        String username = usernameField.getValue();
//        String email = emailField.getValue();
//        String password = passwordField.getValue();
//
//        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
//            Notification.show("All fields are required!");
//            return;
//        }
//
//        try {
//            userRepository.saveUser(username, email, password);
//            Notification.show("User saved successfully!");
//        } catch (Exception e) {
//            Notification.show("Error saving user: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    private Div addBookmark() {

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

        AtomicBoolean isBookmarked = new AtomicBoolean(false);

        emptyImage.addClickListener(event -> {
            if (isBookmarked.get()) {
                emptyImage.setSrc(emptyImageSrc);
                emptyImage.setWidth("50px");
                emptyImage.setHeight("50px");
            } else {
                emptyImage.setSrc(fullImageSrc);
                emptyImage.setWidth("75px");
                emptyImage.setHeight("75px");
            }
            emptyImage.getElement().getStyle().set("display", "block");
            emptyImage.getElement().getStyle().set("margin", "auto");
            isBookmarked.set(!isBookmarked.get());
        });
        return bookmarkDiv;
    }

//    public void beforeEnter(BeforeEnterEvent event) {
//        if (!SecurityService.isAuthenticated()) {
//            event.rerouteTo("login");
//        }
//    }
}