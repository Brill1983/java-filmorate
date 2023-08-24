package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/films")
@RestController
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable long filmId) {
        return filmService.getFilmById(filmId);
    }

    @DeleteMapping("/{filmId}")
    public boolean delete(@PathVariable Long filmId) {
        return filmService.delete(filmId);
    }

    @PostMapping
    public Film addFilm(@RequestBody @Valid Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void userLike(@PathVariable long filmId, @PathVariable long userId) {
        filmService.userLikedFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLike(@PathVariable long filmId, @PathVariable long userId) {
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/{filmId}/likes")
    public List<User> getFIlmLikes(@PathVariable long filmId) {
        return filmService.getFIlmLikes(filmId);
    }

    @GetMapping("/{filmId}/genres")
    public List<Genre> getFilmGenres(@PathVariable long filmId) {
        return filmService.getFilmGenres(filmId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film>  getFilmDirectorsSortedList(@PathVariable int directorId, @RequestParam String sortBy) {
        return filmService.getFilmDirectorsSortedList(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> searchFilm(@RequestParam String query, @RequestParam List<String> by) {
        return filmService.searchFilm(query, by);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(value = "count", defaultValue = "10") Integer count,
                                          @RequestParam(value = "genreId", required = false) Integer genreId,
                                          @RequestParam(value = "year", required = false) Integer year) {
        return filmService.getMostPopularFilms(genreId, year, count);
    }
}
