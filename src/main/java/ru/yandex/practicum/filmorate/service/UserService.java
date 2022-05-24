package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserService {
    private UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }

    public User findUserById(Integer userId) {
        if (userStorage.getUsers().containsKey(userId)) {
            return  userStorage.getUsers().get(userId);
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    public void addFriendById(Integer userId, Integer friendId) {
        if (userStorage.getUsers().containsKey(userId)) {
            User user =  userStorage.getUsers().get(userId);
            if (userStorage.getUsers().containsKey(friendId)) {
                user.getFriends().add(Long.valueOf(friendId));
                user =  userStorage.getUsers().get(friendId);
                user.getFriends().add(Long.valueOf(userId));
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", friendId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    public void deleteFriendById(Integer userId, Integer friendId) {
        if (userStorage.getUsers().containsKey(userId)) {
            User user =  userStorage.getUsers().get(userId);
            if (userStorage.getUsers().containsKey(friendId)) {
                user.getFriends().remove(Long.valueOf(friendId));
                user =  userStorage.getUsers().get(friendId);
                user.getFriends().remove(Long.valueOf(userId));
            } else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
            }
        } else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    public Collection<User> findFriendsById(Integer userId) {
        List<User> friends=new ArrayList<>();
        if (userStorage.getUsers().containsKey(userId)) {
            User user =  userStorage.getUsers().get(userId);
            for (Long friendsId:user.getFriends()){
                user= userStorage.getUsers().get(friendsId.intValue());
                friends.add(user);
            }
            return friends;
        }else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", userId));
        }
    }

    public Collection<User> getFriends(Integer id, Integer otherId) {
        List<User> friends=new ArrayList<>();
        if (userStorage.getUsers().containsKey(id)) {
            if (userStorage.getUsers().containsKey(otherId)){
                User user=  userStorage.getUsers().get(id);
                User friendUser= userStorage.getUsers().get(otherId);
                if(user.getFriends().isEmpty()){
                    return friends;
                }
                if (friendUser.getFriends().isEmpty()){
                    return friends;
                }
                for (Long idUser:user.getFriends()){
                    for (Long idUserFriends:friendUser.getFriends()){
                        if (idUser==idUserFriends){
                            friends.add(userStorage.getUsers().get(idUserFriends.intValue()));
                        }
                    }
                }
                return friends;
            }else {
                throw new UserNotFoundException(String.format("Пользователь %s не найден", otherId));
            }
        }else {
            throw new UserNotFoundException(String.format("Пользователь %s не найден", id));
        }
    }
}


