package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
public class FilmGenresDbStorage {
    private final String FILM_GENRES_MERGE_SQL = "MERGE INTO FILM_GENRES KEY (film_id, genre_id) VALUES (?, ?)";
    private final String FILM_GENRES_GET_SQL = "SELECT * FROM FILM_GENRES fg LEFT JOIN GENRES g ON" +
            " fg.genre_id = g.id WHERE fg.film_id=? ORDER BY g.id";
    private final String FILM_GENRES_DELETE_SQL = "DELETE FROM FILM_GENRES WHERE film_id=?";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmGenresDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(int id, int genreId) {
        jdbcTemplate.update(FILM_GENRES_MERGE_SQL, id, genreId);
    }

    public Collection<Genre> getGenres(int id) {
        Collection<Genre> genres = jdbcTemplate.query(FILM_GENRES_GET_SQL, this::makeGenre, id);
        if (genres.isEmpty()) {
            return null;
        } else {
            return genres;
        }
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }

    public void remove(int id) {
        jdbcTemplate.update(FILM_GENRES_DELETE_SQL, id);
    }
}
