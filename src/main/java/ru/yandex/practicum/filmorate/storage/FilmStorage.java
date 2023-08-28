package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    boolean delete(long filmId);

    Film updateFilm(Film film);

    List<Film> getFilmsList();

    List<Film> getDirectorFilmListByYear(int directorId);

    List<Film> getDirectorFilmListByLikes(int directorId);

    List<Film> searchFilmsByDirAndName(String query);

    List<Film> searchFilmsByName(String query);

    List<Film> searchFilmsByDir(String query);

    Optional<Film> getFilmById(long id);

    List<Genre> getFilmGenres(long id);

    List<Film> getMostPopularFilmsByYearAndGenre(int genreId, int year, int count);

    List<Film> getMostPopularFilmsByYear(int year, int count);

    List<Film> getMostPopularFilmsByGenre(int genreId, int count);

    List<Film> getMostPopularFilms(int count);

    List<Film> findCommonFilms(int userId, int friendId);

    boolean checkFilmById(long filmId);

    List<Film> getRecommendations(long userId);
}
