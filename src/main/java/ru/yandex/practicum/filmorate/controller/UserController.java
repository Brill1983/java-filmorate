package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class UserController {

    private Map<Integer, User> users = new HashMap<>();
    int id = 0;

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public User saveUser(@Valid @RequestBody User user) {
        if (user.getEmail().isBlank() || user.getLogin().isBlank()) {
            log.debug("Передан пользователь с пустым email: {}, или логином: {}", user.getEmail(), user.getLogin());
            throw new ValidationException("Поля email и логин - обязательны к заполнению");
        }
        if (!user.getEmail().contains("@") || user.getLogin().contains(" ")) {
            log.debug("Передан неправильный email: {}, или логином с пробелом: {}", user.getEmail(), user.getLogin());
            throw new ValidationException("Email должен содержать '@', а логин не должен содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Передана неправильная дата рождения пользователя: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Вместо пустого имени присваивается логин");
            user.setName(user.getLogin());
        }
        user.setId(++id);
        users.put(id, user);
        log.debug("Пользователь добавлен: {}", user);
        return users.get(id);
    }

    @PutMapping("/users")
    public User updateUser(@RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            log.debug("В запросе передан пользователь с некорректным ID: {}", user.getId());
            throw new ValidationException("Пользователя с ID " + user.getId() + " нет в базе");
        }
        if (user.getEmail().isBlank() || user.getLogin().isBlank()) {
            log.debug("Передан пользователь с пустым email: {}, или логином: {}", user.getEmail(), user.getLogin());
            throw new ValidationException("Поля email и логин пользователя - обязательны к заполнению");
        }
        if (!user.getEmail().contains("@") || user.getLogin().contains(" ")) {
            log.debug("Передан неправильный email: {}, или логином с пробелом: {}", user.getEmail(), user.getLogin());
            throw new ValidationException("Email должен содержать '@' а логин не должен содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Передана неправильная дата рождения пользователя: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Вместо пустого имени присваивается логин");
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.debug("Пользователь с ID: {}, обновлен: {}", user.getId(), user);
        return users.get(user.getId());
    }

    @GetMapping("/users")
    public List<User> getUserList() {
        log.debug("Количество пользователей в базе {}", users.size());
        return new ArrayList<>(users.values());
    }
}
