package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;


    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public FilmStorage getFilmStorage() {
        return filmStorage;
    }

    public Optional<Film> findFilmById(Integer filmId) {
        return filmStorage.findFilmById(filmId);
    }

    public void findLikeFilmById(Integer id, Integer userId) {
        filmStorage.findLikeFilmById(id, userId);
    }

    public void removeLikeFilmById(Integer id, Integer userId) {
        filmStorage.removeLikeFilmById(id, userId);
    }

    public Collection<Film> getFilmPopularByCount(Integer count) {
        return filmStorage.getFilmPopularByCount(count);
    }
}

