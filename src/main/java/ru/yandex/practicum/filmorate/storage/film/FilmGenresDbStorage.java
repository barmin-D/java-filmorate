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
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmGenresDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(int id, int genreId) {
        String sqlQuery = "MERGE INTO FILM_GENRES KEY (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, id, genreId);
    }

    public Collection<Genre> getGenres(int id) {
        String sql = "SELECT * FROM FILM_GENRES fg LEFT JOIN GENRES g ON fg.genre_id = g.id " +
                "WHERE fg.film_id=? ORDER BY g.id";
        Collection<Genre> genres = jdbcTemplate.query(sql, this::makeGenre, id);
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
        String sqlQuery = "DELETE FROM FILM_GENRES WHERE film_id=?";
        jdbcTemplate.update(sqlQuery, id);
    }
}
