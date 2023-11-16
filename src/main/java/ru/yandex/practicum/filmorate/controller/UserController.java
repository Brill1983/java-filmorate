package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validator.Create;
import ru.yandex.practicum.filmorate.validator.Update;

import java.util.List;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable long userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public boolean delete(@PathVariable Integer userId) {
        return userService.delete(userId);
    }

    @PostMapping
    public User saveUser(@RequestBody @Validated(Create.class) User user) {
        return userService.saveUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody @Validated(Update.class) User user) {
        return userService.updateUser(user);
    }

    @PutMapping(value = "/{id}/friends/{friendId}")
    public boolean addAsFriend(@PathVariable long id, @PathVariable long friendId) {
        return userService.addAsFriend(id, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public boolean deleteFriend(@PathVariable long userId, @PathVariable long friendId) {
        return userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<User> getFriendsList(@PathVariable long userId) {
        return userService.getFriendsList(userId);
    }

    @GetMapping("{userId}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable long userId, @PathVariable long otherId) {
        return userService.getCommonFriends(userId, otherId);
    }

    @GetMapping("/{userId}/feed")
    public List<Event> getUserFeed(@PathVariable int userId) {
        return userService.getUserFeed(userId);
    }

    @GetMapping("/{userId}/recommendations")
    public List<Film> getRecommendations(@PathVariable long userId) {
        return userService.getRecommendations(userId);
    }
}
