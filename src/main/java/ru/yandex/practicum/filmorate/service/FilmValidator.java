package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikesStorage;
import ru.yandex.practicum.filmorate.storage.MpaCategoryStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmValidator {

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private final GenreStorage genreStorage;
    private final MpaCategoryStorage categoryStorage;
    private final FilmStorage filmStorage;
    private final LikesStorage likesStorage;

    public void valid(Film film) {
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
            log.debug("В запросе передан фильм с продолжительностью {}", film.getDuration());
            throw new ValidationException("Продолжительность не может быть 0 или отрицательной");
        }
        List<Genre> filmGenreList = film.getGenres();
        if(!filmGenreList.isEmpty()) {
            List<Genre> genresInDb = genreStorage.findAllGenres();
            for (Genre genre : filmGenreList) {
                if(!genresInDb.contains(genre)) {
                    log.debug("В запросе передан фильм с неправильным id жанра {}", genre.getId());
                    throw new ValidationException("Жанр должен соответствовать базе данных");
                }
            }
        }
        if (film.getCategoryMpa() != null) {
            List<MpaCategory> mpaCategories = categoryStorage.findAllMpaCategories();
            if(!mpaCategories.contains(film.getCategoryMpa())) {
                log.debug("В запросе передан фильм с неправильным id категории MPA {}", film.getCategoryMpa().getId());
                throw new ValidationException("Категория MPA должен соответствовать базе данных");
            }
        }
    }

    public void validId(long id) {
        Optional<Film> film = filmStorage.getFilmById(id);
        if(film.isEmpty()) {
            log.debug("В фильм с ID: {}, отсутствует в базе", id);
            throw new FilmNotFoundException("Фильма с ID " + id + " нет в базе");
        }
    }
}
