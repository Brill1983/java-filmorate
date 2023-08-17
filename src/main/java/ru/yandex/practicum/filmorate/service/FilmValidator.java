package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaCategoryStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmValidator {

    private final GenreStorage genreStorage;
    private final MpaCategoryStorage categoryStorage;
    private final FilmStorage filmStorage;

    public void valid(Film film) {

        if (!film.getGenres().isEmpty()) {
            List<Integer> filmGenreIdList = film.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Integer> genresInDb = genreStorage.findAllGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            for (Integer genreId : filmGenreIdList) {
                if (!genresInDb.contains(genreId)) {
                    log.debug("В запросе передан фильм с неправильным id жанра {}", genreId);
                    throw new ValidationException("Жанр должен соответствовать базе данных");
                }
            }
        }
        if (film.getMpa() != null) {
            List<Integer> mpaCategories = categoryStorage.findAllMpaCategories().stream()
                    .map(MpaCategory::getId)
                    .collect(Collectors.toList());
            if (!mpaCategories.contains(film.getMpa().getId())) {
                log.debug("В запросе передан фильм с неправильным id категории MPA {}", film.getMpa().getId());
                throw new ValidationException("Категория MPA должен соответствовать базе данных");
            }
        }

        // TODO проверка директоров
    }

    public void validId(long id) {
        Optional<Film> film = filmStorage.getFilmById(id);
        if (film.isEmpty()) {
            log.debug("В фильм с ID: {}, отсутствует в базе", id);
            throw new FilmNotFoundException("Фильма с ID " + id + " нет в базе");
        }
    }
}
