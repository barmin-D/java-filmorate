package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Optional<User> getUserById(Integer userId);

    Collection<User> findAll();

    User create(User user);

    User update(User user);

    void addFriendById(Integer userId, Integer friendId);

    void deleteFriendById(Integer userId, Integer friendId);

    Collection<User> findFriendsById(Integer userId);

    Collection<User> getFriends(Integer id, Integer otherId);
}
