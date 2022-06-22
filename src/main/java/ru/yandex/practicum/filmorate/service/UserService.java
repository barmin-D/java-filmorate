package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {
    private UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }

    public Optional<User> findUserById(Integer userId) {
        return userStorage.getUserById(userId);
    }

    public void addFriendById(Integer userId, Integer friendId) {
        userStorage.addFriendById(userId, friendId);
    }

    public void deleteFriendById(Integer userId, Integer friendId) {
        userStorage.deleteFriendById(userId, friendId);
    }

    public Collection<User> findFriendsById(Integer userId) {
        return userStorage.findFriendsById(userId);
    }

    public Collection<User> getFriends(Integer id, Integer otherId) {
        return userStorage.getFriends(id, otherId);
    }
}


