package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
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
    private final ValidationService validationService;
    private final FriendsStorage friendsStorage;
    private final FilmService filmService;


    public User getUserById(long userId) {
        return userStorage.getUserById(userId).orElseThrow(() -> new ObjectNotFoundException("Пользователя с ID " + userId + " нет в базе"));
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }


    public User saveUser(User user) {
        checkUserName(user);
        return userStorage.saveUser(checkUserName(user));
    }

    public User updateUser(User user) {
        validationService.validUserId(user.getId());
        checkUserName(user);
        return userStorage.updateUser(checkUserName(user));
    }

    public boolean delete(long userId) {
        return userStorage.delete(userId);
    }

    public boolean addAsFriend(long userId, long friendId) {
        validationService.validUserId(userId);
        validationService.validUserId(friendId);
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
        validationService.validUserId(userId);
        validationService.validUserId(friendId);
        return friendsStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriendsList(long userId) {
        validationService.validUserId(userId);
        return friendsStorage.getFriendsList(userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        validationService.validUserId(userId);
        validationService.validUserId(otherId);
        return friendsStorage.getCommonFriends(userId, otherId);
    }

    public List<Film> getRecommendations(long userId) {
        validationService.validUserId(userId);
        return filmService.getRecommendations(userId);
    }

    private User checkUserName(User user) {
        if (StringUtils.isBlank(user.getName())) {
            log.debug("Вместо пустого имени присваивается логин");
            user.setName(user.getLogin());
        }
        return user;
    }
}
