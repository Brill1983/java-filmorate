package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendsStorage {

    void addAsFriend(long userId, long friendId);

    boolean deleteFriend(long userId, long friendId);

    List<User> getFriendsList(long id);

    List<User> getCommonFriends(long id, long otherId);

    boolean friendshipCheck(long userId, long friendId);
}
