package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userRepository;
    private final ValidationService validationService;
    private final FriendsStorage friendsRepository;
    private final FilmService filmService;
    private final EventStorage eventRepository;


    public User getUserById(long userId) {
        return userRepository.getUserById(userId).orElseThrow(() -> new ObjectNotFoundException("Пользователя с ID " + userId + " нет в базе"));
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }


    public User saveUser(User user) {
        checkUserName(user);
        return userRepository.saveUser(checkUserName(user));
    }

    public User updateUser(User user) {
        validationService.validUserId(user.getId());
        checkUserName(user);
        return userRepository.updateUser(checkUserName(user));
    }

    public boolean delete(long userId) {
        validationService.validUserId(userId);
        return userRepository.delete(userId);
    }

    public boolean addAsFriend(long userId, long friendId) {
        validationService.validUserId(userId);
        validationService.validUserId(friendId);
        List<Long> friendsIdList = friendsRepository.getFriendsList(userId)
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());
        if (!friendsIdList.contains(friendId)) {
            friendsRepository.addAsFriend(userId, friendId);
            eventRepository.add(new Event(userId, EventType.FRIEND, friendId, Operation.ADD));
            return true;
        }
        log.info("Пользователь с ID {} уже есть друг c ID {}", userId, friendId);
        return false;
    }

    public boolean deleteFriend(long userId, long friendId) {
        validationService.validUserId(userId);
        validationService.validUserId(friendId);
        boolean isDelete = friendsRepository.deleteFriend(userId, friendId);
        if (isDelete) {
            eventRepository.add(new Event(userId, EventType.FRIEND, friendId, Operation.REMOVE));
        }
        return isDelete;
    }

    public List<User> getFriendsList(long userId) {
        validationService.validUserId(userId);
        return friendsRepository.getFriendsList(userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        validationService.validUserId(userId);
        validationService.validUserId(otherId);
        return friendsRepository.getCommonFriends(userId, otherId);
    }

    public List<Event> getUserFeed(long userId) {
        validationService.validUserId(userId);
        return eventRepository.getEventsByUserId(userId);
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
