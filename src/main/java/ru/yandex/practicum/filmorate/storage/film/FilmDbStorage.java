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
    private final LocalDate DATE_FIRST_MOVIE = LocalDate.of(1895, 12, 28);
    private final String FILM_REQUEST_SQL = "SELECT * FROM FILMS WHERE id = ?";
    private final String USER_REQUEST_SQL = "SELECT * FROM USERS WHERE id = ?";
    private final String FILMS_ALL_SQL = "select * from FILMS";
    private final String FILM_INSERT_SQL = "INSERT INTO FILMS (name,release_date,description,duration,rate,mpa_id)" +
            "VALUES(?,?,?,?,?,?)";
    private final String FILM_UPDATE_SQL = "UPDATE FILMS SET " +
            "name =?,release_date=?,description=?,duration=?,rate=?,mpa_id=? WHERE id=?";
    private final String FILM_LIKE_INSERT_SQL = "INSERT INTO FILM_LIKES(film_id,user_id) VALUES (?,?)";
    private final String FILM_LIKE_DELETE_SQL = "DELETE FROM FILM_LIKES WHERE film_id=? AND user_id=?";
    private final String FILM_POPULAR_SQL = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rate," +
            " f.mpa_id, COUNT(fl.film_id) popular FROM FILMS f LEFT JOIN FILM_LIKES fl ON f.id=fl.film_id " +
            "GROUP BY f.id ORDER BY popular DESC LIMIT ?";
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
        return jdbcTemplate.query(FILMS_ALL_SQL, this::makeFilm);
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .rate(rs.getInt("rate"))
                .mpa(mpaStorage.getMpaById(rs.getInt("mpa_id")).get())
                .genres(filmGenresDbStorage.getGenres(rs.getInt("id")))
                .build();
    }

    @Override
    public Film create(Film film) {
        validateFilm(film);
        film.setId(getIdCounter());
        log.debug("фильм записан");
        jdbcTemplate.update(FILM_INSERT_SQL,
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
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(FILM_REQUEST_SQL, film.getId());
        if (filmRows.next()) {
            validateFilm(film);
            log.debug("фильм перезаписан");
            jdbcTemplate.update(FILM_UPDATE_SQL,
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

    private void validateFilm(Film film) {
        if (film.getName().length() == 0) {
            log.error("Запись фильма не удалась, пустое название");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getReleaseDate().isBefore(DATE_FIRST_MOVIE)) {
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
    }

    @Override
    public Optional<Film> getFilmById(Integer filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(FILM_REQUEST_SQL, filmId);
        if (filmRows.next()) {
            log.info("Найден фильм: {} {}", filmRows.getString("id"), filmRows.getString("name"));
            return Optional.of(jdbcTemplate.queryForObject(FILM_REQUEST_SQL, this::makeFilm, filmId));
        } else {
            log.error("Фильм с идентификатором {} не найден.", filmId);
            throw new FilmNotFoundException("Такого фильма нет");
        }
    }

    @Override
    public void putLikeFilmById(Integer id, Integer userId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(FILM_REQUEST_SQL, id);
        if (filmRows.next()) {
            SqlRowSet userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, userId);
            if (userRows.next()) {
                log.debug("Пользователь поставил лайк");
                jdbcTemplate.update(FILM_LIKE_INSERT_SQL, id, userId);
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
            }
        } else {
            throw new FilmNotFoundException("Такого фильма нет");
        }
    }

    @Override
    public void removeLikeFilmById(Integer id, Integer userId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(FILM_REQUEST_SQL, id);
        if (filmRows.next()) {
            SqlRowSet userRows = jdbcTemplate.queryForRowSet(USER_REQUEST_SQL, userId);
            if (userRows.next()) {
                log.debug("Пользователь удалил лайк");
                jdbcTemplate.update(FILM_LIKE_DELETE_SQL, id, userId);
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
            count = 10;
            return jdbcTemplate.query(FILM_POPULAR_SQL, this::makeFilm, count);
        } else {
            return jdbcTemplate.query(FILM_POPULAR_SQL, this::makeFilm, count);
        }
    }
}
