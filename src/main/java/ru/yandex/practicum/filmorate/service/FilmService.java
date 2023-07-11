package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
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
//    private final GenreStorage genreStorage;

    public Film addFilm(Film film) {
        filmValidator.valid(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) { //TODO
        filmValidator.valid(film);
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long id) { // TODO check how works validation
        Film film = filmStorage.getFilmById(id).orElseThrow(() -> new FilmNotFoundException("Фильма с ID " + id + " нет в базе"));
        return film;
    }

    public Film userLikedFilm(long id, long userId) { //TODO
        UserValidator.validId(userId, userStorage);
        filmValidator.validId(id);

//        Film film = filmStorage.getFilm(id);
//        film.getLikes().add(userId);
        log.debug("Фильму {} добавлен лайк от пользователя {}", id, userId);
        return null /*film*/;
    }

    public Film deleteLike(long id, long userId) { //TODO
        UserValidator.validId(userId, userStorage); //TODO исправить после создания UserDbStorage
        filmValidator.validId(id);

//        Film film = filmStorage.getFilm(id);
//        film.getLikes().remove(userId);
        log.debug("У фильма {} удален лайк от пользователя {}", id, userId);
        return null /*film*/;
    }

    public List<Film> getPopularFilmList(int count) { //TODO
        if (count <= 0) {
            log.debug("Было передано некорректное значение count {}, применено значение по умолчанию", count);
            count = 10;
        }
        List<Film> films = filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
        log.debug("По запросу на {} топ фильмов, создан список из топ {} фильмов", count, films.size());
        return films;
    }
}
