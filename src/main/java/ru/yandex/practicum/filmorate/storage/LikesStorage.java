package ru.yandex.practicum.filmorate.storage;

import java.util.List;

public interface LikesStorage {

    List<Long> getUsersIdLikedFilm(int id);
}
