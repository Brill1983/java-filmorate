package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
//import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@Service
@AllArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;
    private final FilmValidator filmValidator;

    public List<Genre> findAllGenres() {
        return genreStorage.findAllGenres();
    }

    public Genre findGenreById(int id) {
        return genreStorage.findGenreById(id).orElseThrow(() -> new GenreNotFoundException("Жанра с ID " + id + " нет в базе"));
    }

    public List<Genre> findGenresByFilmId(int id) {
//        filmValidator.validId(id);
        return genreStorage.findGenresByFilmId(id);
    }

    public Genre addGenre(Genre genre) {
        if (StringUtils.isBlank(genre.getName())) {
//            throw new ValidationException("Название жанра обзятельно для заполнения");
        }
        return genreStorage.createNewGenre(genre);
    }
}
