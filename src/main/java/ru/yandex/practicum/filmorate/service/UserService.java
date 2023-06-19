package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User saveUser(User user) {
        return userStorage.saveUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(long id) {
        return userStorage.getUser(id);
    }

    public User addAsFriend(long userId, long friendId) {
        UserValidator.idPresentInStorage(userId, userStorage);
        UserValidator.idPresentInStorage(friendId, userStorage);

        User user = userStorage.getUser(userId);
        user.getFriends().add(friendId);
        log.debug("Пользователь с ID {}, добавлен в друзья пользователя {}", friendId, userId);
        User friendUser = userStorage.getUser(friendId);
        friendUser.getFriends().add(userId);
        log.debug("Пользователь с ID {}, добавлен в друзья пользователя {}", userId, friendId);
        return user;
    }

    public User deleteFriend(long userId, long friendId) {
        UserValidator.idPresentInStorage(userId, userStorage);
        UserValidator.idPresentInStorage(friendId, userStorage);

        User user = userStorage.getUser(userId);
        user.getFriends().remove(friendId);
        log.debug("Пользователь с ID {}, удален из друзей пользователя {}", friendId, userId);
        User friendUser = userStorage.getUser(friendId);
        friendUser.getFriends().remove(userId);
        log.debug("Пользователь с ID {}, удален из друзей пользователя {}", userId, friendId);
        return user;
    }

    public List<User> getFriendsList(long id) {
        UserValidator.idPresentInStorage(id, userStorage);

        List<User> friendsList = new ArrayList<>();
        User user = userStorage.getUser(id);
        for (Long friendId : user.getFriends()) {
            friendsList.add(userStorage.getUser(friendId));
        }
        log.debug("У пользователя с ID {}, в списке {} друзей", id, friendsList.size());
        return friendsList;
    }

    public List<User> getCommonFriends(long id, long otherId) {
        UserValidator.idPresentInStorage(id, userStorage);
        UserValidator.idPresentInStorage(otherId, userStorage);

        Set<Long> intersection = new HashSet<>(userStorage.getUser(id).getFriends());
        Set<Long> otherUserFriends = userStorage.getUser(otherId).getFriends();
        intersection.retainAll(otherUserFriends);
        log.debug("У пользователей с ID {} и {}, в списке {} общих друзей", id, otherId, intersection.size());
        return intersection.stream()
                .map(userId -> userStorage.getUser(userId))
                .collect(Collectors.toList());
    }
}
