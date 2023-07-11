package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class GenreService {

    private GenreStorage genreStorage;
    private FilmValidator filmValidator;

    public List<Genre> findAllGenres() {
        return genreStorage.findAllGenres();
    }

    public Genre findGenreById(int id) { // TODO check how works validation
        Genre genre = genreStorage.findGenreById(id).orElseThrow(() -> new GenreNotFoundException("Жанра с ID " + id + " нет в базе"));
        return genre;
    }

    public List<Genre> findGenresByFilmId(int id) { // TODO check how works validation
        filmValidator.validId(id);
        return genreStorage.findGenresByFilmId(id);
    }

    public Genre addGenre(Genre genre) { // TODO check how works validation
        if(StringUtils.isBlank(genre.getName())) {
            throw new ValidationException("Название жанра обзятельно для заполнения");
        }
        return genreStorage.createNewGenre(genre);
    }
}
