package webapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import webapp.entities.Movie;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

@Service
@Getter
public class MovieService {
    private webapp.entities.Movie movie;
    private ObjectMapper objectMapper;
    private static final String API_KEY = "AIzaSyAn0NWE1Tj6-og7Uyj1kyYko1cLBPdhn7o";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    public Movie SendRequest(String title) {
        objectMapper = new ObjectMapper();

        try {
            char replacement = '+';
            title = title.replace(' ', replacement);
            URL url = new URL("http://www.omdbapi.com/?t=" + title + "&apikey=8bc18b62");
            System.out.println(url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    informationString.append(scanner.nextLine());
                }

                scanner.close();

                this.movie = objectMapper.readValue(informationString.toString(), Movie.class);
                System.out.println("Movie Details: " + movie);
                return movie;
            }

        } catch (MalformedURLException e) {
            System.out.println("Wrong URL format");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static String getTrailer(String title) {
        String query = title + " trailer";
        OkHttpClient client = new OkHttpClient();

        String url = YOUTUBE_SEARCH_URL + "?part=snippet&q=" + query.replace(" ", "+") + "&key=" + API_KEY + "&maxResults=1";
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
}