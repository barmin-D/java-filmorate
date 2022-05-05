package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@RestController
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private Map<Integer, Film> films = new HashMap<>();

    @GetMapping("/films")
    public Map findAll() {
        log.debug("Текущее количество фильмов: {}", films.size());
        return films;
    }

    @PostMapping(value = "/films")
    public Film create(@RequestBody Film film) {
        if (film.getName().length() == 0) {
            log.error("запись фильма не удалась, пустое название");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() <= 0 || film.getDescription().length() >= 200) {
            log.error(" запись фильма не удалась, превышен лимит символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        LocalDate localDateStart = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(localDateStart)) {
            log.error("запись фильма не удалась, дата релиза до 28-12-1895 ");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration().isNegative()) {
            log.error(" запись фильма не удалась, отрицательная продолжительность фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
        log.debug("фильм записан");
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping(value = "/films")
    public Film update(@RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            if (film.getName().length() == 0) {
                log.error("запись фильма не удалась, пустое название");
                throw new ValidationException("Название не может быть пустым");
            }
            if (film.getDescription().length() > 200) {
                log.error(" запись фильма не удалась, превышен лимит символов");
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            LocalDate localDate = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate().isBefore(localDate)) {
                log.error("запись фильма не удалась, дата релиза до 28-12-1895 ");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            }
            if (film.getDuration().isNegative()) {
                log.error(" запись фильма не удалась, отрицательная продолжительность фильма");
                throw new ValidationException("Продолжительность фильма должна быть положительной");
            }
            log.debug("фильм перезаписан");
            films.put(film.getId(), film);
        } else {
            throw new ValidationException("Такого фильма нет");
        }
        return film;
    }
}
