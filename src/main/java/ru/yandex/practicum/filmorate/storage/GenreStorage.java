package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {

    List<Genre> findAllGenres();

    Optional<Genre> findGenreById(int id);

    List<Genre> findGenresByFilmId(int id);

    Genre createNewGenre(Genre genre);

    List<Integer> findGenresIdsByFilmId(int id);

    void addGenresForFilm(Integer genreId, Long filmId);
}
