package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmRepository;
    private final ValidationService validationService;

    private final LikesStorage likesRepository;
    private final GenreStorage genreStorage;
    private final MpaCategoryStorage mpaCategoryStorage;
    private final DirectorRepository directorRepository;


    public Film getFilmById(long filmId) {
        return filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new FilmNotFoundException("Фильма с ID " + filmId + " нет в базе"));
    }

    public List<Film> getAllFilms() {
        // TODO лайки - в репозитории, отдельным методом, и передачей в makeFilm
        return filmRepository.getFilmsList(); // TODO - нужно ли проверять на пустой список?
    }

    public Film addFilm(Film film) {
        validationService.validFilm(film);
        Film backedFilm =  filmRepository.addFilm(film);
        return getFilmById(backedFilm.getId());
    }

    public boolean delete(long filmId) {
        return filmRepository.delete(filmId);
    }

    public Film updateFilm(Film film) {
        validationService.validFilmId(film.getId());
        validationService.validFilm(film);
        Film backedFilm = filmRepository.updateFilm(film);
        return getFilmById(backedFilm.getId());
    }

    public void userLikedFilm(long filmId, long userId) {
        validationService.validUserId(userId);
        validationService.validFilmId(filmId);
        if (likesRepository.checkUserLikedFilm(filmId, userId)) {
            log.info("У фильма с ID {} уже есть лайк от пользователя {}", filmId, userId);
        } else {
            likesRepository.userLikedFilm(filmId, userId);
            log.info("Фильму с ID {} добавлен лайк от пользователя {}", filmId, userId);
        }
    }

    public void deleteLike(long filmId, long userId) {
        validationService.validUserId(userId);
        validationService.validFilmId(filmId);

        if (likesRepository.checkUserLikedFilm(filmId, userId)) {
            log.info("У фильма с ID {} есть лайк от пользователя {}, можно смело удалять", filmId, userId);
            likesRepository.deleteLike(filmId, userId);
        } else {
            log.info("У фильма с ID {} нет лайка от пользователя {}, удалять нечего", filmId, userId);
        }
    }

    public List<User> getFIlmLikes(long filmId) {
        validationService.validFilmId(filmId);
        return likesRepository.getFilmLikes(filmId);
    }

    public List<Genre> getFilmGenres(long filmId) {
        validationService.validFilmId(filmId);
        return filmRepository.getFilmGenres(filmId);
    }

    public List<Film> getFilmDirectorsSortedList(int directorId, String sortBy) {
        validationService.validDirectorId(directorId);
        List<Film> filmList;
        if (sortBy.equals("year")) {
            filmList = filmRepository.getDirectorFilmListByYear(directorId);
        } else if (sortBy.equals("likes")) {
            filmList = filmRepository.getDirectorFilmListByLikes(directorId);
        } else {
            throw new IncorrectParameterException("В запрос передан неправильный параметр, нужен 'like' или 'year'");
        }
        return filmList;
    }

    public List<Film> searchFilm(String query, List<String> by) {
        if (by.isEmpty() || by.size() > 2) {
            throw new IncorrectParameterException("В запрос азпрос должено быть передано не менее 1 и не более 2 пораметров для выборки('director' и/или 'title')");
        }
        if (by.size() == 1 && by.contains("title")) {
            return filmRepository.searchFilmsByName(query);
        }
        if (by.size() == 1 && by.contains("director")) {
            return filmRepository.searchFilmsByDir(query);
        }
        if (by.contains("title") && by.contains("director")) {
            return filmRepository.searchFilmsByDirAndName(query);
        }
        throw new IncorrectParameterException("В запрос передан неправильный параметр, нужен 'director' и/или 'title'");
    }

    public List<Film> getMostPopularFilms(Integer genreId, Integer year, Integer count) {
        if (genreId != null) {
            validationService.validGenreId(genreId);
            if (year != null) {
                return filmRepository.getMostPopularFilmsByYearAndGenre(genreId, year, count);
            } else {
                return filmRepository.getMostPopularFilmsByGenre(genreId, count);
            }
        } else if (year != null) {
            return filmRepository.getMostPopularFilmsByYear(year, count);
        } else {
            return filmRepository.getMostPopularFilms(count);
        }
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        validationService.validUserId(userId);
        validationService.validUserId(friendId);
        return filmRepository.findCommonFilms(userId, friendId);
    }
}
