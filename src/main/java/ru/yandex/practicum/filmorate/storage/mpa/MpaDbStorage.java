package ru.yandex.practicum.filmorate.storage.mpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Component
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(MpaDbStorage.class);
    private final String MPA_REQUEST_SQL = "SELECT * FROM MPA WHERE id = ?";
    private final String MPA_ALL = "select * from MPA";

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> findAllMpa() {

        return jdbcTemplate.query(MPA_ALL, this::makeMpa);
    }

    @Override
    public Optional<Mpa> getMpaById(Integer mpaId) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(MPA_REQUEST_SQL, mpaId);
        if (mpaRows.next()) {
            return Optional.of(jdbcTemplate.queryForObject(MPA_REQUEST_SQL, this::makeMpa, mpaId));
        } else {
            log.error("Рейтинг фильма  с идентификатором {} не найден.", mpaId);
            throw new FilmNotFoundException("Такого рейтинга нет");
        }
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}
