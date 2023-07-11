package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikesStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmValidator filmValidator;
    private final UserValidator userValidator;
    private final LikesStorage likesStorage;


    public Film addFilm(Film film) {
        filmValidator.valid(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        filmValidator.validId(film.getId());
        filmValidator.valid(film);
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long filmId) {
        return filmStorage.getFilmById(filmId).orElseThrow(() -> new FilmNotFoundException("Фильма с ID " + filmId + " нет в базе"));
    }

    public void userLikedFilm(long filmId, long userId) {
        userValidator.validId(userId);
        filmValidator.validId(filmId);
        if(likesStorage.checkUserLikedFilm(filmId, userId)) {
            log.info("У фильма с ID {} уже есть лайк от пользователя {}", filmId, userId);
        } else {
            likesStorage.userLikedFilm(filmId, userId);
            log.info("Фильму с ID {} добавлен лайк от пользователя {}", filmId, userId);
        }
    }

    public void deleteLike(long filmId, long userId) {
        userValidator.validId(userId);
        filmValidator.validId(filmId);

        if(likesStorage.checkUserLikedFilm(filmId, userId)) {
            log.info("У фильма с ID {} есть лайк от пользователя {}, можно смело удалять", filmId, userId);
            likesStorage.deleteLike(filmId, userId);
        } else {
            log.info("У фильма с ID {} нет лайка от пользователя {}, удалять нечего", filmId, userId);
        }
    }


    public List<Film> getPopularFilmList(int count) { //TODO
        if (count <= 0) {
            log.debug("Было передано некорректное значение count {}, применено значение по умолчанию", count);
            count = 10;
        }
        return filmStorage.getPopularFilmList(count);
    }
}
