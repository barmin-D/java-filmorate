package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    Film film;
    User user;

    @Test
    void findAll() {
        film = new Film(1, "test", LocalDate.of(2010, 2, 2), "testing", 100,
                4, new Mpa(5, "NC-17"), null);
        filmDbStorage.create(film);
        film = new Film(2, "test", LocalDate.of(2010, 2, 2), "testing", 100,
                4, new Mpa(5, "NC-17"), null);
        filmDbStorage.create(film);
        film = new Film(3, "test", LocalDate.of(2010, 2, 2), "testing", 100,
                4, new Mpa(5, "NC-17"), null);
        filmDbStorage.create(film);

        Collection<Film> filmCollection = filmDbStorage.findAll();

        assertEquals(filmCollection.size(), 5, "неправельное количество фильмов");

    }

    @Test
    void create() {
        film = new Film(1, "test", LocalDate.of(2010, 2, 2), "testing", 100,
                4, new Mpa(5, "NC-17"), null);
        filmDbStorage.create(film);

        Optional<Film> filmOptional = filmDbStorage.findFilmById(1);

        assertEquals(filmOptional.get().getName(), film.getName(), "запись не удалась");
    }

    @Test
    void update() {
        film = new Film(1, "test", LocalDate.of(2010, 2, 2), "testing", 100,
                4, new Mpa(5, "NC-17"), null);
        filmDbStorage.create(film);
        film = new Film(1, "testllll", LocalDate.of(2010, 2, 2), "testing", 100,
                4, new Mpa(5, "NC-17"), null);
        filmDbStorage.update(film);

        Optional<Film> filmOptional = filmDbStorage.findFilmById(1);

        assertEquals(filmOptional.get().getName(), "testllll", "запись не удалась");
    }

    @Test
    void findFilmById() {
        film = new Film(1, "test", LocalDate.of(2010, 2, 2), "testing", 100,
                4, new Mpa(5, "NC-17"), null);
        filmDbStorage.create(film);
        Optional<Film> filmOptional = filmDbStorage.findFilmById(1);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    void findLikeFilmById() {
        film = new Film(1, "test", LocalDate.of(2010, 2, 2), "testing", 100,
                4, new Mpa(5, "NC-17"), null);
        filmDbStorage.create(film);
        user = new User(1, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        filmDbStorage.findLikeFilmById(1, 1);
        Collection<Film> popular = filmDbStorage.getFilmPopularByCount(1);
        assertEquals(popular.size(), 1, "лайк не поставился");
    }
}