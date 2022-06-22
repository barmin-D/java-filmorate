package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private static int idCounter = 1;
    private final LocalDate LOCAL_DATA_START = LocalDate.of(1895, 12, 28);
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private Map<Integer, Film> films = new HashMap<>();


    public static int getIdCounter() {
        return idCounter++;
    }

    @Override
    public Map<Integer, Film> getFilms() {
        return films;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        if (film.getName().length() == 0) {
            log.error("Запись фильма не удалась, пустое название");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() <= 0 || film.getDescription().length() >= 200) {
            log.error("Запись фильма не удалась, превышен лимит символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LOCAL_DATA_START)) {
            log.error("Запись фильма не удалась, дата релиза до 28-12-1895 ");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.error("Запись фильма не удалась, отрицательная продолжительность фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        film.setId(getIdCounter());
        log.debug("фильм записан");
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            if (film.getName().length() == 0) {
                log.error("Запись фильма не удалась, пустое название");
                throw new ValidationException("Название не может быть пустым");
            }
            if (film.getDescription().length() > 200) {
                log.error("Запись фильма не удалась, превышен лимит символов");
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            LocalDate localDate = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate().isBefore(localDate)) {
                log.error("Запись фильма не удалась, дата релиза до 28-12-1895 ");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            }
            if (film.getDuration() < 0) {
                log.error("Запись фильма не удалась, отрицательная продолжительность фильма");
                throw new ValidationException("Продолжительность фильма должна быть положительной");
            }
            log.debug("фильм перезаписан");
            films.put(film.getId(), film);
        } else {
            throw new FilmNotFoundException("Такого фильма нет");
        }
        return film;
    }

    @Override
    public Optional<Film> findFilmById(Integer filmId) {
        if (getFilms().containsKey(filmId)) {
            return Optional.ofNullable(getFilms().get(filmId));
        } else {
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", filmId));
        }
    }

    @Override
    public void findLikeFilmById(Integer id, Integer userId) {
    }

    @Override
    public void removeLikeFilmById(Integer id, Integer userId) {
    }

    @Override
    public Collection<Film> getFilmPopularByCount(Integer count) {
        return null;
    }
}

