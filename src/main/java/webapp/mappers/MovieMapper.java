package webapp.mappers;

import org.mapstruct.Mapper;
import webapp.entities.Movie;
import webapp.entities.MovieDB;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    MovieDB mapMovieDB(Movie movie);
}

