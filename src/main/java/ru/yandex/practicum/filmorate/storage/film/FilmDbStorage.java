package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private static int idCounter = 1;
    private final LocalDate LOCAL_DATA_START = LocalDate.of(1895, 12, 28);
    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmGenresDbStorage filmGenresDbStorage;
    private final MpaStorage mpaStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, @Qualifier("UserDbStorage") UserStorage userStorage,
                         FilmGenresDbStorage filmGenresDbStorage, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmGenresDbStorage = filmGenresDbStorage;
        this.mpaStorage = mpaStorage;
    }

    public static int getIdCounter() {
        return idCounter++;
    }

    @Override
    public Map<Integer, Film> getFilms() {
        return null;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "select * from FILMS";
        return jdbcTemplate.query(sql, this::makeFilm);
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .rate(rs.getInt("rate"))
                .mpa(mpaStorage.findMpaById(rs.getInt("mpa_id")).get())
                .genres(filmGenresDbStorage.getGenres(rs.getInt("id")))
                .build();
    }

    @Override
    public Film create(Film film) {
        if (film.getName().length() == 0) {
            log.error("Запись фильма не удалась, пустое название");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getReleaseDate().isBefore(LOCAL_DATA_START)) {
            log.error("Запись фильма не удалась, дата релиза до 28-12-1895 ");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDescription().length() <= 0 || film.getDescription().length() >= 200) {
            log.error("Запись фильма не удалась, превышен лимит символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getDuration() < 0) {
            log.error("Запись фильма не удалась, отрицательная продолжительность фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        if (film.getMpa() == null) {
            log.error("Запись фильма не удалась, ошибка mpa");
            throw new ValidationException("ошибка mpa");
        }
        film.setId(getIdCounter());
        log.debug("фильм записан");
        String sqlQuery = "INSERT INTO FILMS (name,release_date,description,duration,rate,mpa_id)" +
                "VALUES(?,?,?,?,?,?)";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getReleaseDate(),
                film.getDescription(),
                film.getDuration(),
                film.getRate(),
                film.getMpa().getId());
        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                filmGenresDbStorage.create(film.getId(), g.getId());
            }
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE id = ?", film.getId());
        if (filmRows.next()) {
            if (film.getName().length() == 0) {
                log.error("Запись фильма не удалась, пустое название");
                throw new ValidationException("Название не может быть пустым");
            }
            if (film.getReleaseDate().isBefore(LOCAL_DATA_START)) {
                log.error("Запись фильма не удалась, дата релиза до 28-12-1895 ");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            }
            if (film.getDescription().length() <= 0 || film.getDescription().length() >= 200) {
                log.error("Запись фильма не удалась, превышен лимит символов");
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            if (film.getDuration() < 0) {
                log.error("Запись фильма не удалась, отрицательная продолжительность фильма");
                throw new ValidationException("Продолжительность фильма должна быть положительной");
            }
            log.debug("фильм перезаписан");
            String sqlQuery = "UPDATE FILMS SET " +
                    "name =?,release_date=?,description=?,duration=?,rate=?,mpa_id=?" +
                    "WHERE id=?";
            jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getReleaseDate(),
                    film.getDescription(),
                    film.getDuration(),
                    film.getRate(),
                    film.getMpa().getId(),
                    film.getId());
            filmGenresDbStorage.remove(film.getId());
            if (film.getGenres() != null) {
                Set<Integer> genres = new TreeSet<>();
                for (Genre g : film.getGenres()) {
                    genres.add(g.getId());
                    filmGenresDbStorage.create(film.getId(), g.getId());
                }
                film.getGenres().clear();
                for (Integer id : genres) {
                    film.getGenres().add(new Genre(id, null));
                }
            }
            return film;
        } else {
            throw new FilmNotFoundException("Такого фильма нет");
        }
    }

    @Override
    public Optional<Film> findFilmById(Integer filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE id = ?", filmId);
        if (filmRows.next()) {
            log.info("Найден фильм: {} {}", filmRows.getString("id"), filmRows.getString("name"));
            String sql = "SELECT * FROM FILMS WHERE id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, this::makeFilm, filmId));
        } else {
            log.info("Фильм с идентификатором {} не найден.", filmId);
            throw new FilmNotFoundException("Такого фильма нет");
        }
    }

    @Override
    public void findLikeFilmById(Integer id, Integer userId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE id = ?", id);
        if (filmRows.next()) {
            SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", userId);
            if (userRows.next()) {
                log.debug("Пользователь поставил лайк");
                String sqlQuery = "INSERT INTO FILM_LIKES(film_id,user_id) VALUES (?,?)";
                jdbcTemplate.update(sqlQuery, id, userId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
            }
        } else {
            throw new FilmNotFoundException("Такого фильма нет");
        }
    }

    @Override
    public void removeLikeFilmById(Integer id, Integer userId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE id = ?", id);
        if (filmRows.next()) {
            SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE id = ?", userId);
            if (userRows.next()) {
                log.debug("Пользователь удалил лайк");
                String sqlQuery = "DELETE FROM FILM_LIKES WHERE film_id=? AND user_id=?";
                jdbcTemplate.update(sqlQuery, id, userId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
            }
        } else {
            throw new FilmNotFoundException("Такого фильма нет");
        }
    }

    @Override
    public Collection<Film> getFilmPopularByCount(Integer count) {
        if (count == null) {
            String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rate, f.mpa_id," +
                    " COUNT(fl.film_id) popular FROM FILMS f LEFT JOIN FILM_LIKES fl ON f.id=fl.film_id " +
                    "GROUP BY f.id ORDER BY popular DESC LIMIT 10";
            return jdbcTemplate.query(sql, this::makeFilm);
        } else {
            String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rate, f.mpa_id," +
                    " COUNT(fl.film_id) popular FROM FILMS f LEFT JOIN FILM_LIKES fl ON f.id=fl.film_id " +
                    "GROUP BY f.id ORDER BY popular DESC LIMIT ?";
            return jdbcTemplate.query(sql, this::makeFilm, count);
        }
    }
}
