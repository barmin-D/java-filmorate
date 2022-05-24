package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    @Test
    void findAll() {
        UserController userController = new UserController(new UserService(new InMemoryUserStorage()));
        User user = new User("tortiss", "Dmitry", "toritss00@yandex.ru",
                LocalDate.of(1992, 03, 23));
        userController.create(user);

        assertEquals(1, userController.findAll().size(), "Неверное количество задач.");
    }

    @Test
    void shouldValidationExceptionLogin() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        UserController userController = new UserController(new UserService(new InMemoryUserStorage()));
                        User user = new User("torti ss", "Dmitry", "toritss00@yandex.ru",
                                LocalDate.of(1992, 03, 23));
                        userController.create(user);
                    }
                });
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldValidationExceptionLoginNull() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        UserController userController = new UserController(new UserService(new InMemoryUserStorage()));
                        User user = new User("", "Dmitry", "toritss00@yandex.ru",
                                LocalDate.of(1992, 03, 23));
                        userController.create(user);
                    }
                });
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldValidationExceptionEmail() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        UserController userController = new UserController(new UserService(new InMemoryUserStorage()));
                        User user = new User("tortiss", "Dmitry", "toritss00yandex.ru",
                                LocalDate.of(1992, 03, 23));
                        userController.create(user);
                    }
                });
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @",
                exception.getMessage());
    }

    @Test
    void shouldValidationExceptionEmailNull() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        UserController userController = new UserController(new UserService(new InMemoryUserStorage()));
                        User user = new User("tortiss", "Dmitry", "",
                                LocalDate.of(1992, 03, 23));
                        userController.create(user);
                    }
                });
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @",
                exception.getMessage());
    }

    @Test
    void shouldValidationExceptionBirthday() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        UserController userController = new UserController(new UserService(new InMemoryUserStorage()));
                        User user = new User("tortiss", "Dmitry", "tortiss@yandex.ru",
                                LocalDate.of(2992, 03, 23));
                        userController.create(user);
                    }
                });
        assertEquals("дата рождения не может быть в будущем", exception.getMessage());
    }
}