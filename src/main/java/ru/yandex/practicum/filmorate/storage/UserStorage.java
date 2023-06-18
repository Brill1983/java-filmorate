package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {

    User saveUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    User getUser(long id);

    Set<Long> getUsersIds();
}
