package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserValidator userValidator;
    private final FriendsStorage friendsStorage;

    public User saveUser(User user) {
        userValidator.valid(user);
        return userStorage.saveUser(checkUserName(user));
    }

    public User updateUser(User user) {
        userValidator.validId(user.getId());
        userValidator.valid(user);
        return userStorage.updateUser(checkUserName(user));
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(long userId) {
        return userStorage.getUserById(userId).orElseThrow(() -> new UserNotFoundException("Пользователя с ID " + userId + " нет в базе"));
    }

    public boolean addAsFriend(long userId, long friendId) {
        userValidator.validId(userId);
        userValidator.validId(friendId);
        List<Long> friendsIdList = friendsStorage.getFriendsList(userId)
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());
        if (!friendsIdList.contains(friendId)) {
            friendsStorage.addAsFriend(userId, friendId);
            return true;
        }
        log.info("Пользователь с ID {} уже есть друг c ID {}", userId, friendId);
        return false;
    }

    public boolean deleteFriend(long userId, long friendId) {
        userValidator.validId(userId);
        userValidator.validId(friendId);
        return friendsStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriendsList(long userId) {
        userValidator.validId(userId);
        return friendsStorage.getFriendsList(userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        userValidator.validId(userId);
        userValidator.validId(otherId);
        return friendsStorage.getCommonFriends(userId, otherId);
    }

    private User checkUserName(User user) {
        if (StringUtils.isBlank(user.getName())) {
            log.debug("Вместо пустого имени присваивается логин");
            user.setName(user.getLogin());
        }
        return user;
    }
}
