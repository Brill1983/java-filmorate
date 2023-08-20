package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;
    private final UserValidator userValidator;
    private final LikesStorage likesStorage;
    private final GenreStorage genreStorage;
    private final MpaCategoryStorage mpaCategoryStorage;
    private final DirectorRepository directorRepository;

    public Film addFilm(Film film) {
        filmValidator.valid(film);
        Film backedFilm =  filmStorage.addFilm(film);

        for (Genre genre : film.getGenres()) {
            genreStorage.addGenresForFilm(genre.getId(), backedFilm.getId());
        }
        // TODO добавить режиссеров
        return getFilmById(backedFilm.getId());
    }

    public Film updateFilm(Film film) {
        filmValidator.validId(film.getId());
        filmValidator.valid(film);
        Film backedFilm = filmStorage.updateFilm(film);

        genreStorage.deleteGenresOfFilm(film.getId());
        for (Genre genre : film.getGenres()) {
            genreStorage.addGenresForFilm(genre.getId(), backedFilm.getId());
        }

        // TODO добавить режиссеров
        return getFilmById(backedFilm.getId());
    }

    public List<Film> getAllFilms() {
        List<Film> filmList = filmStorage.getAllFilms();

            // TODO лайки - в репозитории, отдельным методом, и передачей в makeFilm
            // TODO режиссеры - в репозитории, отдельным методом, и передачей в makeFilm
        return filmList; // TODO - нужно ли проверять на пустой список?
    }

    public Film getFilmById(long filmId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new FilmNotFoundException("Фильма с ID " + filmId + " нет в базе"));

        //TODO режиссеры
        //TODO лайки
        Set<Genre> filmGenres = new HashSet<>(genreStorage.findGenresByFilmId(filmId));
        film.setGenres(filmGenres);
        return film;
    }

    public void userLikedFilm(long filmId, long userId) {
        userValidator.validId(userId);
        filmValidator.validId(filmId);
        if (likesStorage.checkUserLikedFilm(filmId, userId)) {
            log.info("У фильма с ID {} уже есть лайк от пользователя {}", filmId, userId);
        } else {
            likesStorage.userLikedFilm(filmId, userId);
            log.info("Фильму с ID {} добавлен лайк от пользователя {}", filmId, userId);
        }
    }

    public void deleteLike(long filmId, long userId) {
        userValidator.validId(userId);
        filmValidator.validId(filmId);

        if (likesStorage.checkUserLikedFilm(filmId, userId)) {
            log.info("У фильма с ID {} есть лайк от пользователя {}, можно смело удалять", filmId, userId);
            likesStorage.deleteLike(filmId, userId);
        } else {
            log.info("У фильма с ID {} нет лайка от пользователя {}, удалять нечего", filmId, userId);
        }
    }


    public List<Film> getPopularFilmList(int count) {
        if (count <= 0) {
            log.debug("Было передано некорректное значение count {}, применено значение по умолчанию", count);
            count = 10;
        }
        return filmStorage.getPopularFilmList(count);
    }
}
