package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

@Slf4j
public class UserValidator {

    public static void valid(User user) {
        if (StringUtils.isBlank(user.getEmail()) || StringUtils.isBlank(user.getLogin())) {
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
        if (StringUtils.isBlank(user.getName())) {
            log.debug("Вместо пустого имени присваивается логин");
            user.setName(user.getLogin());
        }
    }

    public static void idPresentInStorage(long userId, UserStorage userStorage) {
        if (!userStorage.getUsersIds().contains(userId)) {
            log.debug("Пользователь с ID: {}, не зарегистрирован", userId);
            throw new UserNotFoundException("Пользователя с ID " + userId + " нет в базе");
        }
    }

    public static void validId(long id) {
        if (id <= 0) {
            log.debug("В запросе передан пользователь с некорректным ID: {}", id);
            throw new UserNotFoundException("В запросе передан пользователь с некорректным ID:" + id);
        }
    }

}
