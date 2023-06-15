package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Service
@Slf4j
public class UserService {

    public User addAsFriend(Long userId, Long friendId, UserStorage userStorage){
        if (!userStorage.getUsersIds().contains(userId)) {
            log.debug("В запросе передан пользователь с некорректным ID: {}", userId);
            throw new ValidationException("Пользователя с ID " + userId + " нет в базе");
        }
        if (!userStorage.getUsersIds().contains(friendId)) {
            log.debug("В запросе передан друг пользователя с некорректным ID: {}", friendId);
            throw new ValidationException("Друга пользователя с ID " + friendId + " нет в базе");
        }
        User user = userStorage.getUser(userId);
        user.getFriends().add(friendId);
        return user;
    }

}
