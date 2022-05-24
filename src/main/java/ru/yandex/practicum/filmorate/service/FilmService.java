package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;


    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public FilmStorage getFilmStorage() {
        return filmStorage;
    }

    public Film findFilmById(Integer filmId) {
        if (filmStorage.getFilms().containsKey(filmId)) {
            return filmStorage.getFilms().get(filmId);
        } else {
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", filmId));
        }
    }

    public void findLikeFilmById(Integer id, Integer userId) {
        if (filmStorage.getFilms().containsKey(id)) {
            if (userStorage.getUsers().containsKey(userId)) {
                Film film = filmStorage.getFilms().get(id);
                film.getLike().add(Long.valueOf(userId));
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
            }
        } else {
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", id));
        }
    }

    public void removeLikeFilmById(Integer id, Integer userId) {
        if (filmStorage.getFilms().containsKey(id)) {
            if (userStorage.getUsers().containsKey(userId)) {
                Film film = filmStorage.getFilms().get(id);
                film.getLike().remove(Long.valueOf(userId));
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
            }
        } else {
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", id));
        }
    }

    public Collection<Film> getFilmPopularByCount(Integer count) {
        if (count == null) {
            List<Film> list = filmStorage.findAll().stream()
                    .sorted(new SizeComparator() {
                        @Override
                        public int compare(Film f1, Film f2) {
                            return super.compare(f1, f2);
                        }
                    })
                    .limit(10)
                    .collect(Collectors.toList());
            return list;
        } else {
            List<Film> list = filmStorage.findAll().stream()
                    .sorted(new SizeComparator() {
                        @Override
                        public int compare(Film f1, Film f2) {
                            return super.compare(f1, f2);
                        }
                    })
                    .limit(count)
                    .collect(Collectors.toList());
            return list;
        }
    }
}

abstract class SizeComparator implements Comparator<Film> {

    public int compare(Film f1, Film f2) {
        if (f1.getLike().size() == f2.getLike().size()) {
            return 0;
        }
        if (f1.getLike().size() < f2.getLike().size()) {
            return 1;
        } else {
            return -1;
        }
    }
}

