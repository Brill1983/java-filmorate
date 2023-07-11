package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface LikesStorage {

    void userLikedFilm(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    boolean checkUserLikedFilm (long filmId, long userId);
}
