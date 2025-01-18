package webapp.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Movies")
public class MovieDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String poster;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Watched> watchedMovies;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
}