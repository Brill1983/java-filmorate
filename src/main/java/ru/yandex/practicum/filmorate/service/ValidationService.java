package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exceptions.IncorrectRequestBodyException;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final GenreStorage genreStorage;
    private final MpaCategoryStorage categoryStorage;
    private final FilmStorage filmStorage;
    private final DirectorStorage directorStorage;
    private final UserStorage userStorage;
    private final ReviewStorage reviewStorage;

    public void validFilm(Film film) {

        if (!film.getGenres().isEmpty()) {
            List<Integer> genresIdList = genreStorage.findAllGenresIds();

            film.getGenres().forEach(genre -> {
                if (!genresIdList.contains(genre.getId())) {
                    log.debug("В запросе передан фильм с неправильным id жанра {}", genre.getId());
                    throw new IncorrectRequestBodyException("Жанр должен соответствовать базе данных");
                }
            });
        }
        if (film.getMpa() != null) {
            List<Integer> mpaCategories = categoryStorage.findAllMpaIds();
            if (!mpaCategories.contains(film.getMpa().getId())) {
                log.debug("В запросе передан фильм с неправильным id категории MPA {}", film.getMpa().getId());
                throw new IncorrectRequestBodyException("Категория MPA должен соответствовать базе данных");
            }
        }
        if (!film.getDirectors().isEmpty()) {
            List<Integer> dirIdList = directorStorage.findAllDirectorsIds();

            film.getDirectors().forEach(director -> {
                if (!dirIdList.contains(director.getId())) {
                    log.debug("В запросе передан фильм с неправильным id режиссера {}", director.getId());
                    throw new IncorrectRequestBodyException("Режиссер должен соответствовать базе данных");
                }
            });
        }
    }

    public void validUserId(long userId) {
        Optional<User> user = userStorage.getUserById(userId);
        if (user.isEmpty()) {
            log.debug("Пользователь с ID: {}, отсутствует в базе", userId);
            throw new ObjectNotFoundException("Пользователя с ID " + userId + " нет в базе");
        }
    }

    public void validFilmId(long filmId) {
        if (!filmStorage.checkFilmById(filmId)) {
            log.debug("В фильм с ID: {}, отсутствует в базе", filmId);
            throw new ObjectNotFoundException("Фильма с ID " + filmId + " нет в базе");
        }
    }

    public void validDirectorId(int directorId) {
        Optional<Director> director = directorStorage.getDirectorById(directorId);
        if (director.isEmpty()) {
            log.debug("Режиссер с ID: {}, отсутствует в базе", directorId);
            throw new ObjectNotFoundException("Режиссера с ID " + directorId + " нет в базе");
        }
    }

    public void validGenreId(Integer genreId) {
        Optional<Genre> genre = genreStorage.findGenreById(genreId);
        if (genre.isEmpty()) {
            log.debug("Жанр с ID: {}, отсутствует в базе", genreId);
            throw new IncorrectParameterException("Жанра с ID " + genreId + " нет в базе");
        }
    }

    public void validReviewAuthor(long reviewId, long userId) {
        validUserId(userId);
        Optional<Review> receivedReview = reviewStorage.getReviewById(reviewId);
        if (receivedReview.isEmpty()) {
            log.debug("Отзыв с ID: {}, отсутствует в базе", reviewId);
            throw new ObjectNotFoundException("Отзыва с ID " + reviewId + " нет в базе");
        }

        if (receivedReview.get().getUserId() == userId) {
            log.debug("Отзыв с ID: {}, написан пользователем с ID {}, нельзя оценивать свой отзыв", reviewId, userId);
            throw new IncorrectParameterException("Отзыв с ID " + reviewId + ", написан пользователем с ID " +
                    userId + ", нельзя оценивать свой отзыв");
        }
    }
}
