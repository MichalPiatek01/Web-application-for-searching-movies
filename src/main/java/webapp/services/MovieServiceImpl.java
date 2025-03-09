package webapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.html.Span;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import webapp.configs.ApiConfig;
import webapp.entities.Movie;
import webapp.entities.MovieDB;
import webapp.repositories.MovieRepository;

import java.util.Map;
import java.util.Objects;

@Service
@Getter
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final ApiConfig apiConfig;
    private webapp.entities.Movie movie;
    private ObjectMapper objectMapper;
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    public MovieServiceImpl(MovieRepository movieRepository, ApiConfig apiConfig) {
        this.movieRepository = movieRepository;
        this.apiConfig = apiConfig;
    }

    public Movie SendRequest(String title) throws MovieNotFoundException {
        objectMapper = new ObjectMapper();
        OkHttpClient client = new OkHttpClient();

        try {
            String omdbKey = apiConfig.getOmdbApiKey();
            title = title.replace(' ', '+');
            String url = "http://www.omdbapi.com/?t=" + title + "&plot=full" + "&apikey=" + omdbKey;
            System.out.println(url);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("HttpResponseCode: " + response.code());
                }

                assert response.body() != null;
                String responseBody = response.body().string();
                Map responseMap = objectMapper.readValue(responseBody, Map.class);

                if ("False".equals(responseMap.get("Response"))) {
                    throw new MovieNotFoundException("Movie not found: " + title);
                }

                this.movie = objectMapper.readValue(responseBody, Movie.class);
                System.out.println("Movie Details: " + movie);
                return movie;
            }
        } catch (MovieNotFoundException e) {
            System.out.println(e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return null;
    }

    public String getTrailer(String title) {
        String query = title + " trailer";
        OkHttpClient client = new OkHttpClient();
        String youtubeKey = apiConfig.getYoutubeApiKey();

        String url = YOUTUBE_SEARCH_URL + "?part=snippet&q=" + query.replace(" ", "+")
                + "&key=" + youtubeKey + "&maxResults=1";
        System.out.println("Query URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Response Code: " + response.code());

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println("Response Body: " + responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray items = jsonResponse.optJSONArray("items");

                if (items != null && items.length() > 0) {
                    JSONObject firstItem = items.getJSONObject(0);
                    JSONObject idObject = firstItem.getJSONObject("id");

                    if ("youtube#video".equals(idObject.optString("kind"))) {
                        String videoId = idObject.getString("videoId");
                        return "https://www.youtube.com/watch?v=" + videoId;
                    } else {
                        System.out.println("First item is not a video.");
                    }
                } else {
                    System.out.println("No items found in the response.");
                }
            } else {
                System.out.println("Response not successful or body is null.");
            }
        } catch (Exception e) {
            System.out.println("Error while searching YouTube: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public MovieDB findByTitle(String title) {
        return movieRepository.findByTitle(title);
    }

    @Override
    public boolean doesMovieExist(String title) {
        return movieRepository.existsByTitle(title);
    }

    @Override
    public void save(MovieDB movieDB) {
        movieRepository.save(movieDB);
    }

    @Override
    public Span getMovieInfo(Movie movie) {
        String year = movie.getYear();
        String rated = movie.getRated();
        String runtime = movie.getRuntime();
        String genre = movie.getGenre();
        Span movieDetails = new Span();
        if (Objects.equals(year, "N/A") & Objects.equals(rated, "N/A") & Objects.equals(runtime, "N/A") &
                Objects.equals(genre, "N/A")) {

            movieDetails.add("Movie details not available.");
        } else {
            movieDetails.add(year + " " + rated + " " + runtime + " " + genre);
        }
        return movieDetails;
    }

    public static class MovieNotFoundException extends Exception {
        public MovieNotFoundException(String message) {
            super(message);
        }
    }
}