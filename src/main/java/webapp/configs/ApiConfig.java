package webapp.configs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ApiConfig {

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

}