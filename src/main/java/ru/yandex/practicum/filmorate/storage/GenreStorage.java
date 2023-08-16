package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GenreStorage {

    List<Genre> findAllGenres();

    Optional<Genre> findGenreById(int id);

    List<Genre> findGenresByFilmId(long id);

    Genre createNewGenre(Genre genre);

    boolean deleteGenresOfFilm(long id);

    void addGenresForFilm(Integer genreId, Long filmId);

    Map<Genre, List<Long>> getFilmsGenresMap();
}
