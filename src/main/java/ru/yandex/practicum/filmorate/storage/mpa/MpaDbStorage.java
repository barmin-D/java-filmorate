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

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> findAllMpa() {
        String sql = "select * from MPA";
        return jdbcTemplate.query(sql, this::makeMpa);
    }

    @Override
    public Optional<Mpa> findMpaById(Integer mpaId) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE id = ?", mpaId);
        if (mpaRows.next()) {
            String sql = "SELECT * FROM MPA WHERE id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, this::makeMpa, mpaId));
        } else {
            log.info("Рейтинг фильма  с идентификатором {} не найден.", mpaId);
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
