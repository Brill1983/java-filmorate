package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/films")
@RestController
@Slf4j
public class FilmController {

    private int id = 0;
    private Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        FilmValidator.valid(film);
        film.setId(++id);
        films.put(id, film);
        log.debug("Фильм с названием '{}' и идентификатором '{}' добавлен", film.getName(), film.getId());
        return films.get(id);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            log.debug("В запросе передан фильм с некорректным ID: {}", film.getId());
            throw new ValidationException("Фильма с ID " + film.getId() + " в базе не существует");
        }
        FilmValidator.valid(film);
        films.put(film.getId(), film);
        log.debug("Фильм с идентификатором {} обновлен", film.getId());
        return films.get(film.getId());
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Количество фильмов в базе {}", films.size());
        return new ArrayList<>(films.values());
    }
}
