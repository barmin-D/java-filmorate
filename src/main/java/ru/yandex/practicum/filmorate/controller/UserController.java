package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Optional;

@RestController
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public Collection<User> findAll() {
        return userService.getUserStorage().findAll();
    }

    @PostMapping(value = "/users")
    public User create(@RequestBody User user) {
        return userService.getUserStorage().create(user);
    }

    @PutMapping(value = "/users")
    public User update(@RequestBody User user) {
        return userService.getUserStorage().update(user);
    }

    @GetMapping("/users/{userId}")
    public Optional<User> findUser(@PathVariable("userId") Integer userId) {
        return userService.findUserById(userId);
    }

    @PutMapping(value = "/users/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Integer userId, @PathVariable("friendId") Integer friendId) {
        userService.addFriendById(userId, friendId);
    }

    @DeleteMapping(value = "/users/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") Integer userId, @PathVariable("friendId") Integer friendId) {
        userService.deleteFriendById(userId, friendId);
    }

    @GetMapping("/users/{id}/friends")
    public Collection<User> findFriends(@PathVariable("id") Integer userId) {
        return userService.findFriendsById(userId);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public Collection<User> getFriends(@PathVariable("id") Integer id, @PathVariable("otherId") Integer otherId) {
        return userService.getFriends(id, otherId);
    }
}
