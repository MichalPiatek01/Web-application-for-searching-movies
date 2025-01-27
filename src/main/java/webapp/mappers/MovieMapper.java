package webapp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import webapp.entities.Movie;
import webapp.entities.MovieDB;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    @Mapping(target = "title", source = "titleFromResponse")
    MovieDB mapMovieDB(Movie movie);
}
