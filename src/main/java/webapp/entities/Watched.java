package webapp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name = "Watched")
public class Watched {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long watchedId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_watched"))
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movie_watched"))
    private MovieDB movie;
}