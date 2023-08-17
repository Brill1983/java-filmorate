package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;
    private final UserValidator userValidator;
    private final LikesStorage likesStorage;
    private final GenreStorage genreStorage;
    private final MpaCategoryStorage mpaCategoryStorage;
    private final DirectorRepository directorRepository;

    public Film addFilm(Film film) {
        filmValidator.valid(film);
        Film backedFilm =  filmStorage.addFilm(film);

        List<Integer> filmGenresIdList = film.getGenres().stream()// добавляем жанры
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());
        for (Integer genreId : filmGenresIdList) {
            genreStorage.addGenresForFilm(genreId, backedFilm.getId());
        }

        // TODO добавить режиссеров
        return getFilmById(backedFilm.getId());
    }

    public Film updateFilm(Film film) {
        filmValidator.validId(film.getId());
        filmValidator.valid(film);
        Film backedFilm = filmStorage.updateFilm(film);

        genreStorage.deleteGenresOfFilm(film.getId());
        List<Integer> filmGenresIdList = film.getGenres().stream()// добавляем жанры
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());
        for (Integer genreId : filmGenresIdList) {
            genreStorage.addGenresForFilm(genreId, backedFilm.getId());
        }

        // TODO добавить режиссеров
        return getFilmById(backedFilm.getId());
    }

    public List<Film> getAllFilms() {
        List<Film> filmList = filmStorage.getAllFilms();
        if(!filmList.isEmpty()) {

            Map<Genre, List<Long>> filmsGenresMap = genreStorage.getFilmsGenresMap();
            for (Film film : filmList) { // итерация по списку фильмов
                List<Genre> filmGenres = new ArrayList<>();
                for (Genre genre : filmsGenresMap.keySet()) { // итерация по мапе, где ключ: жанр, значение: список с ID фильмов.
                    if (filmsGenresMap.get(genre).contains(film.getId())) { // если ID фильма содержится в списке-значении мапы - то формируем список жанров для фильма
                        filmGenres.add(genre);
                    }
                }
                filmGenres.sort(Comparator.comparing(Genre::getId)); // сортируем список
                film.setGenres(filmGenres);
            }

            return filmList;
        }
        return new ArrayList<>();
    }

    public Film getFilmById(long filmId) {
        Film film = filmStorage.getFilmById(filmId).orElseThrow(() -> new FilmNotFoundException("Фильма с ID " + filmId + " нет в базе"));

//        List<Genre> filmGenres = new ArrayList<>(); // собираем ему жанры TODO МАПА не нужна - можно одним запросом получить список
//        Map<Genre, List<Long>> filmsGenresMap = genreStorage.getFilmsGenresMap();
//        for (Genre genre : filmsGenresMap.keySet()) {
//            if(filmsGenresMap.get(genre).contains(film.getId())) {
//                filmGenres.add(genre);
//            }
//        }
//        filmGenres.sort(Comparator.comparing(Genre::getId));

        List<Genre> filmGenres = genreStorage.findGenresByFilmId(filmId);
        film.setGenres(filmGenres);
        return film;
    }

    public void userLikedFilm(long filmId, long userId) {
        userValidator.validId(userId);
        filmValidator.validId(filmId);
        if (likesStorage.checkUserLikedFilm(filmId, userId)) {
            log.info("У фильма с ID {} уже есть лайк от пользователя {}", filmId, userId);
        } else {
            likesStorage.userLikedFilm(filmId, userId);
            log.info("Фильму с ID {} добавлен лайк от пользователя {}", filmId, userId);
        }
    }

    public void deleteLike(long filmId, long userId) {
        userValidator.validId(userId);
        filmValidator.validId(filmId);

        if (likesStorage.checkUserLikedFilm(filmId, userId)) {
            log.info("У фильма с ID {} есть лайк от пользователя {}, можно смело удалять", filmId, userId);
            likesStorage.deleteLike(filmId, userId);
        } else {
            log.info("У фильма с ID {} нет лайка от пользователя {}, удалять нечего", filmId, userId);
        }
    }


    public List<Film> getPopularFilmList(int count) {
        if (count <= 0) {
            log.debug("Было передано некорректное значение count {}, применено значение по умолчанию", count);
            count = 10;
        }
        return filmStorage.getPopularFilmList(count);
    }
}
