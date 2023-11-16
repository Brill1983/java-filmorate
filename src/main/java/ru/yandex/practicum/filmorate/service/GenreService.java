package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectRequestBodyException;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@Service
@AllArgsConstructor
public class GenreService {

    private final GenreStorage genreRepository;

    public List<Genre> findAllGenres() {
        return genreRepository.findAllGenres();
    }

    public Genre findGenreById(int id) {
        return genreRepository.findGenreById(id).orElseThrow(() -> new ObjectNotFoundException("Жанра с ID " + id + " нет в базе"));
    }

    public Genre addGenre(Genre genre) {
        if (StringUtils.isBlank(genre.getName())) {
            throw new IncorrectRequestBodyException("Название жанра обзятельно для заполнения");
        }
        return genreRepository.createNewGenre(genre);
    }
}
