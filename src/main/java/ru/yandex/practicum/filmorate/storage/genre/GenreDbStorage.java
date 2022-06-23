package ru.yandex.practicum.filmorate.storage.genre;

import org.apache.tomcat.util.net.AprEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final String GENRES_ALL_SQL = "select * from GENRES";
    private final String GENRES_SQL = "SELECT * FROM GENRES WHERE id = ?";
    private final Logger log = LoggerFactory.getLogger(GenreDbStorage.class);

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Genre> findAllGenres() {
        return jdbcTemplate.query(GENRES_ALL_SQL, this::makeGenre);
    }

    @Override
    public Optional<Genre> getGenreById(Integer genreId) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(GENRES_SQL, genreId);
        if (genreRows.next()) {
            return Optional.of(jdbcTemplate.queryForObject(GENRES_SQL, this::makeGenre, genreId));
        } else {
            log.error("Жанр с идентификатором {} не найден.", genreId);
            throw new FilmNotFoundException("Такого жанра нет");
        }
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}
