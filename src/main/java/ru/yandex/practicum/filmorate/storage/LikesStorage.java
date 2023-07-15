package ru.yandex.practicum.filmorate.storage;

public interface LikesStorage {

    void userLikedFilm(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    boolean checkUserLikedFilm(long filmId, long userId);
}
