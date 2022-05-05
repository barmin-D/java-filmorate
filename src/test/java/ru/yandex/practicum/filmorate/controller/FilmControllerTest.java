package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    @Test
    void findAll() {
        FilmController filmController = new FilmController();
        Film film = new Film("Тор", "Новые приключения тора",
                LocalDate.of(2015, 11, 20), Duration.ofHours(2));
        filmController.create(film);

        assertEquals(1, filmController.findAll().size(), "Неверное количество задач.");
    }

    @Test
    void shouldValidationExceptionName() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        FilmController filmController = new FilmController();
                        Film film = new Film("", "Новые приключения тора",
                                LocalDate.of(2015, 11, 20), Duration.ofHours(2));
                        filmController.create(film);
                    }
                });
        assertEquals("Название не может быть пустым", exception.getMessage());
    }


    @Test
    void shouldValidationExceptionDescriptionMin() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        FilmController filmController = new FilmController();
                        Film film = new Film("Tor", "",
                                LocalDate.of(2015, 11, 20), Duration.ofHours(2));
                        filmController.create(film);
                    }
                });
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void shouldValidationExceptionDescriptionMax() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        FilmController filmController = new FilmController();
                        Film film = new Film("Tor", "ffffffffffffffffffffffffffffffffffffff" +
                                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                                "fffffffffffffffffffffffffffffffffffffffffffffffffffff",
                                LocalDate.of(2015, 11, 20), Duration.ofHours(2));
                        filmController.create(film);
                    }
                });
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void shouldValidationExceptionData() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        FilmController filmController = new FilmController();
                        Film film = new Film("Tor", "Test",
                                LocalDate.of(1111, 11, 20), Duration.ofHours(2));
                        filmController.create(film);
                    }
                });
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldValidationExceptionDuration() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        FilmController filmController = new FilmController();
                        Film film = new Film("Tor", "Test",
                                LocalDate.of(2000, 11, 20), Duration.ofHours(-2));
                        filmController.create(film);
                    }
                });
        assertEquals("Продолжительность фильма должна быть положительной", exception.getMessage());
    }
}