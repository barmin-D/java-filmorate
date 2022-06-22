package ru.yandex.practicum.filmorate.storage.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private static int idCounter = 1;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private Map<Integer, User> users = new HashMap<>();


    public Map<Integer, User> getUsers() {
        return users;
    }

    @Override
    public Optional<User> getUserById(Integer userId) {
        return Optional.empty();
    }

    @Override
    public Collection<User> findAll() {
        log.debug("Текущее количество пользователей: {}", users.size());
        return users.values();
    }

    public static int getIdCounter() {
        return idCounter++;
    }

    @Override
    public User create(User user) {
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
            log.error("Дата рождения не может быть в будущем");
            throw new ValidationException("дата рождения не может быть в будущем");
        }
        user.setId(getIdCounter());
        log.debug("Пользователь сохранен");
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            if (user.getLogin().length() == 0 || user.getLogin().contains(" ")) {
                log.error("Логин не может быть пустым и содержать пробелы");
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
            if (user.getName().length() == 0) {
                log.debug("Имя заменилось логином");
            }
            if (user.getEmail().length() == 0 || !user.getEmail().contains("@")) {
                log.error("Электронная почта не может быть пустой и должна содержать символ @");
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            }
            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.error("Дата рождения не может быть в будущем");
                throw new ValidationException("дата рождения не может быть в будущем");
            }
            log.debug("Пользователь изменен");
            users.put(user.getId(), user);
        } else {
            log.error("Такого пользователя нет");
            throw new UserNotFoundException("Такого пользователя нет");
        }
        return user;
    }

    @Override
    public void addFriendById(Integer userId, Integer friendId) {
        if (getUsers().containsKey(userId)) {
            User user = getUsers().get(userId);
            if (getUsers().containsKey(friendId)) {
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", friendId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    @Override
    public void deleteFriendById(Integer userId, Integer friendId) {
        if (getUsers().containsKey(userId)) {
            User user = getUsers().get(userId);
            if (getUsers().containsKey(friendId)) {
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    @Override
    public Collection<User> findFriendsById(Integer userId) {
        return null;
    }

    @Override
    public Collection<User> getFriends(Integer id, Integer otherId) {
        return null;
    }
}
