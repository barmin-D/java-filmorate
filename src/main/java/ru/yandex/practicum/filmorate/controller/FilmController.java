package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Optional;

@RestController
public class FilmController {
    private FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/films")
    public Collection<Film> findAll() {
        return filmService.getFilmStorage().findAll();
    }

    @PostMapping(value = "/films")
    public Film create(@RequestBody Film film) {
        return filmService.getFilmStorage().create(film);
    }

    @PutMapping(value = "/films")
    public Film update(@RequestBody Film film) {
        return filmService.getFilmStorage().update(film);
    }

    @GetMapping("/films/{filmId}")
    public Optional<Film> findFilm(@PathVariable("filmId") Integer filmId) {
        return filmService.getFilmById(filmId);
    }

    @PutMapping(value = "/films/{id}/like/{userId}")
    public void likeFilm(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        filmService.putLikeFilmById(id, userId);
    }

    @DeleteMapping(value = "/films/{id}/like/{userId}")
    public void removeLikeFilm(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        filmService.removeLikeFilmById(id, userId);
    }

    @GetMapping(value = "/films/popular")
    public Collection<Film> getFilms(@RequestParam(required = false) Integer count) {
        return filmService.getFilmPopularByCount(count);
    }
}


