package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userDbStorage;
    private User user;

    @Test
    public void testFindUserById() {
        user = new User(1, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);

        Optional<User> userOptional = userDbStorage.getUserById(1);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    void findAll() {
        user = new User(1, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        Collection<User> userCollection = userDbStorage.findAll();

        assertEquals(userCollection.size(), 6, "Количество пользователей не совпало");
    }

    @Test
    void create() {
        user = new User(1, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        Collection<User> userCollection = userDbStorage.findAll();

        assertEquals(userCollection.size(), 5, "запись не удалась");
    }

    @Test
    void update() {
        user = new User(1, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        user = new User(1, "tortisыыыыы", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.update(user);

        Optional<User> user2 = userDbStorage.getUserById(1);

        assertEquals(user2.get().getLogin(), "tortisыыыыы", "не обновилось");
    }

    @Test
    void addFriendById() {
        user = new User(1, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        user = new User(2, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        userDbStorage.addFriendById(1, 2);
        Collection<User> userCollection = userDbStorage.findFriendsById(1);
        assertEquals(userCollection.size(), 1, "не добавилось в друзья");
    }

    @Test
    void deleteFriendById() {
        user = new User(1, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        user = new User(2, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        userDbStorage.addFriendById(1, 2);
        userDbStorage.deleteFriendById(1, 2);
        Collection<User> userCollection = userDbStorage.findFriendsById(1);
        assertEquals(userCollection.size(), 0, "не  удалилось из друзья");
    }

    @Test
    void findFriendsById() {
        user = new User(1, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        user = new User(2, "tortis", "dmitry", "tortiss00@yandex.ru",
                LocalDate.of(1992, 4, 23));
        userDbStorage.create(user);
        userDbStorage.addFriendById(1, 2);
        Collection<User> userCollection = userDbStorage.findFriendsById(1);
        assertEquals(userCollection.size(), 2, "нет обьектов в друзьях");
    }
}