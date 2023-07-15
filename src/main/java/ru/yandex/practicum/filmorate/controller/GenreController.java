package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RequestMapping("/genres")
@RestController
@Slf4j
@AllArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public List<Genre> findAllGenres() {
        return genreService.findAllGenres();
    }

    @GetMapping("/{id}")
    Genre findGenreById(@PathVariable int id) {
        return genreService.findGenreById(id);
    }

    @GetMapping("/film/{id}")
    public List<Genre> findGenresByFilmId(@PathVariable int id) {
        return genreService.findGenresByFilmId(id);
    }

    @PostMapping
    public Genre addGenre(@RequestBody Genre genre) {
        return genreService.addGenre(genre);
    }
}
