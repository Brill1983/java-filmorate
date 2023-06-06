package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserValidator;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {

    private Map<Integer, User> users = new HashMap<>();
    private int id = 0;

    @PostMapping
    public User saveUser(@Valid @RequestBody User user) {
        UserValidator.valid(user);
        user.setId(++id);
        users.put(id, user);
        log.debug("Пользователь добавлен: {}", user);
        return users.get(id);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            log.debug("В запросе передан пользователь с некорректным ID: {}", user.getId());
            throw new ValidationException("Пользователя с ID " + user.getId() + " нет в базе");
        }
        UserValidator.valid(user);
        users.put(user.getId(), user);
        log.debug("Пользователь с ID: {}, обновлен: {}", user.getId(), user);
        return users.get(user.getId());
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Количество пользователей в базе {}", users.size());
        return new ArrayList<>(users.values());
    }
}
