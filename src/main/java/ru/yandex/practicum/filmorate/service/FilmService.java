package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
public class FilmService {

    private FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film userLikedFilm(long id, long userId) {
        UserValidator.validId(userId);
        FilmValidator.validId(id, filmStorage);

        Film film = filmStorage.getFilm(id);
        film.getLikes().add(userId);
        log.debug("Фильму {} добавлен лайк от пользователя {}", id, userId);
        return film;
    }

    public Film deleteLike(long id, long userId) {
        UserValidator.validId(userId);
        FilmValidator.validId(id, filmStorage);

        Film film = filmStorage.getFilm(id);
        film.getLikes().remove(userId);
        log.debug("У фильма {} удален лайк от пользователя {}", id, userId);
        return film;
    }

    public List<Film> getPopularFilmList(int count) {
        if (count <= 0) {
            log.debug("Было передано некорректное значение count {}, применено значение по умолчанию", count);
            count = 10;
        }
        List<Film> films = filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
        log.debug("По запросу на {} топ фильмов, cформирован список из топ {} фильмов", count, films.size());
        return films;
    }
}
