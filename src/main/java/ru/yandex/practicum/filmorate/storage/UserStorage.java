package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    User saveUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(long id);

//    Set<Long> getUsersIds();
}
