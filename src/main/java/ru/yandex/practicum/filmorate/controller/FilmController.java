package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
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
        if (film.getName().isBlank() || film.getName() == null) {
            log.debug("В запросе передан фильм с пустым названием");
            throw new ValidationException("Название фильма - обязательно к заполнению");
        }
        if (film.getDescription().length() > 200) {
            log.debug("В запросе передан фильм с описанием более 200 символов");
            throw new ValidationException("Длина описания не должна превышать 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.debug("В запросе передан фильм с датой релиза ранее 28.12.1895");
            throw new ValidationException("Дата релиза фильма не может быть ранее 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            log.debug("В запросе передан фильм с с продолжительностью {}", film.getDuration());
            throw new ValidationException("Продолжительность не может быть 0 или отрицательной");
        }
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
        if (film.getName().isBlank() || film.getName() == null) {
            log.debug("В запросе передан фильм с пустым названием");
            throw new ValidationException("Название фильма - обязательно к заполнению");
        }
        if (film.getDescription().length() > 200) {
            log.debug("В запросе передан фильм с описанием более 200 символов");
            throw new ValidationException("Длина описания не должна превышать 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.debug("В запросе передан фильм с датой релиза ранее 28.12.1895");
            throw new ValidationException("Дата релиза фильма не может быть ранее 28.12.1985");
        }
        if (film.getDuration() <= 0) {
            log.debug("В запросе передан фильм с с продолжительностью {}", film.getDuration());
            throw new ValidationException("Продолжительность не может быть 0 или отрицательной");
        }
        films.put(film.getId(), film);
        log.debug("Фильм с идентификатором {} добавлен", film.getId());
        return films.get(film.getId());
    }

    @GetMapping
    public List<Film> getFilmList() {
        log.debug("Количество фильмов в базе {}", films.size());
        return new ArrayList<>(films.values());
    }
}
