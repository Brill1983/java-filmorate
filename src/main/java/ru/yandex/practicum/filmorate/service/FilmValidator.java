package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;

@Slf4j
public class FilmValidator {

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public static void valid(Film film) {
        if (StringUtils.isBlank(film.getName())) {
            log.debug("В запросе передан фильм с пустым названием");
            throw new ValidationException("Название фильма - обязательно к заполнению");
        }
        if (film.getDescription().length() > 200) {
            log.debug("В запросе передан фильм с описанием более 200 символов");
            throw new ValidationException("Длина описания не должна превышать 200 символов");
        }
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.debug("В запросе передан фильм с датой релиза ранее 28.12.1895");
            throw new ValidationException("Дата релиза фильма не может быть ранее 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            log.debug("В запросе передан фильм с с продолжительностью {}", film.getDuration());
            throw new ValidationException("Продолжительность не может быть 0 или отрицательной");
        }
    }

    public static void validId(long id, FilmStorage filmStorage) {
        if (!filmStorage.getFilmsIds().contains(id)) {
            log.debug("В фильм с ID: {}, отсутствует в базе", id);
            throw new FilmNotFoundException("Фильма с ID " + id + " нет в базе");
        }
    }
}
