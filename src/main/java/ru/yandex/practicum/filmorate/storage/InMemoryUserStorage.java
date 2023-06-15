package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserValidator;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage{

    private Map<Long, User> users = new HashMap<>();

    private long id = 0L;

    @Override
    public User saveUser(User user) {
        UserValidator.valid(user);
        user.setId(++id);
        users.put(id, user);
        log.debug("Пользователь добавлен: {}", user);
        return users.get(id);
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.debug("В запросе передан пользователь с некорректным ID: {}", user.getId());
            throw new ValidationException("Пользователя с ID " + user.getId() + " нет в базе");
        }
        UserValidator.valid(user);
        users.put(user.getId(), user);
        log.debug("Пользователь с ID: {}, обновлен: {}", user.getId(), user);
        return users.get(user.getId());
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Количество пользователей в базе {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(Long id) {
        return users.get(id);
    }

    @Override
    public Set<Long> getUsersIds() {
        return users.keySet();
    }
}
