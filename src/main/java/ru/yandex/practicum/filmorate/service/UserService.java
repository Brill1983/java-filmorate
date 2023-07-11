package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserValidator userValidator;

    public User saveUser(User user) {
        userValidator.valid(user);
        return userStorage.saveUser(user);
    }

    public User updateUser(User user) {
        userValidator.validId(user.getId());
        userValidator.valid(user);
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(long userId) {
        return userStorage.getUserById(userId).orElseThrow(() -> new UserNotFoundException("Пользователя с ID " + userId + " нет в базе"));
    }

    public User addAsFriend(long userId, long friendId) {
        userValidator.validId(userId);
        userValidator.validId(friendId);

//        User user = userStorage.getUserById(userId);
//        user.getFriends().add(friendId);
//        log.debug("Пользователь с ID {}, добавлен в друзья пользователя {}", friendId, userId);
//        User friendUser = userStorage.getUserById(friendId);
//        friendUser.getFriends().add(userId);
//        log.debug("Пользователь с ID {}, добавлен в друзья пользователя {}", userId, friendId);
        return null;
    }

    public User deleteFriend(long userId, long friendId) {
//        UserValidator.validId(userId, userStorage);
//        UserValidator.validId(friendId, userStorage);

//        User user = userStorage.getUserById(userId);
//        user.getFriends().remove(friendId);
//        log.debug("Пользователь с ID {}, удален из друзей пользователя {}", friendId, userId);
//        User friendUser = userStorage.getUserById(friendId);
//        friendUser.getFriends().remove(userId);
//        log.debug("Пользователь с ID {}, удален из друзей пользователя {}", userId, friendId);
        return null;
    }

    public List<User> getFriendsList(long id) {
//        UserValidator.validId(id, userStorage);

//        List<User> friendsList = new ArrayList<>();
//        User user = userStorage.getUserById(id);
//        for (Long friendId : user.getFriends()) {
//            friendsList.add(userStorage.getUserById(friendId));
//        }
//        log.debug("У пользователя с ID {}, в списке {} друзей", id, friendsList.size());
        return null;
    }

    public List<User> getCommonFriends(long id, long otherId) {
//        UserValidator.validId(id, userStorage);
//        UserValidator.validId(otherId, userStorage);

//        Set<Long> intersection = new HashSet<>(userStorage.getUserById(id).getFriends());
//        Set<Long> otherUserFriends = userStorage.getUserById(otherId).getFriends();
//        intersection.retainAll(otherUserFriends);
//        log.debug("У пользователей с ID {} и {}, в списке {} общих друзей", id, otherId, intersection.size());
//        return intersection.stream()
//                .map(userId -> userStorage.getUserById(userId))
//                .collect(Collectors.toList());
        return null;
    }
}
