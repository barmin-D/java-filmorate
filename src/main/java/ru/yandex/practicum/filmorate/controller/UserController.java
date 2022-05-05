package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private Map<Integer, User> users = new HashMap<>();

    @GetMapping("/users")
    public Map findAll() {
        log.debug("Текущее количество пользователей: {}", users.size());
        return users;
    }

    @PostMapping(value = "/users")
    public User create(@RequestBody User user) {
        if (user.getLogin().length() == 0 || user.getLogin().contains(" ")) {
            log.error("Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName().length() == 0) {
            log.debug("Имя заменилось логином");
            user.setName(user.getLogin());
        }
        if (user.getEmail().length() == 0 || !user.getEmail().contains("@")) {
            log.error("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("дата рождения не может быть в будущем");
            throw new ValidationException("дата рождения не может быть в будущем");
        }
        log.debug("Пользователь сохранен");
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping(value = "/users")
    public User update(@RequestBody User user) {
        if (users.containsKey(user.getId())) {
            if (user.getLogin().length() == 0 || user.getLogin().contains(" ")) {
                log.error("Логин не может быть пустым и содержать пробелы");
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
            if (user.getName().length() == 0) {
                log.debug("Имя заменилось логином");
                user.setName(user.getLogin());
            }
            if (user.getEmail().length() == 0 || !user.getEmail().contains("@")) {
                log.error("Электронная почта не может быть пустой и должна содержать символ @");
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            }
            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.error("дата рождения не может быть в будущем");
                throw new ValidationException("дата рождения не может быть в будущем");
            }
            log.debug("Пользователь изменен");
            users.put(user.getId(), user);
        } else {
            log.error("Такого пользователя нет");
            throw new ValidationException("Такого пользователя нет");
        }
        return user;
    }
}
