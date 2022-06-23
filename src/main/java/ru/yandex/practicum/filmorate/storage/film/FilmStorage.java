package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface FilmStorage {

    Map<Integer, Film> getFilms();

    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> getFilmById(Integer filmId);

    void putLikeFilmById(Integer id, Integer userId);

    void removeLikeFilmById(Integer id, Integer userId);

    Collection<Film> getFilmPopularByCount(Integer count);
}
