package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface LikesStorage {

    void userLikedFilm(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    boolean checkUserLikedFilm(long filmId, long userId);

    List<User> getFilmLikes(long filmId);
}
