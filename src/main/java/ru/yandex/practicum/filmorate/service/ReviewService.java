package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exceptions.IncorrectRequestBodyException;
import ru.yandex.practicum.filmorate.exceptions.LikeAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    //    private final EventRepository eventRepository;
    private final ValidationService validationService;

    public Review addReview(Review review) {
        validationService.validUserId(review.getUserId());
        validationService.validFilmId(review.getFilmId());


//        eventRepository.add(EventRepository.createEvent(review.getUserId(),
//                EventType.REVIEW,
//                review.getReviewId(),
//                Operation.ADD));
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        Review reviewFromDb = getReviewById(review.getReviewId());
        validationService.validUserId(review.getUserId());
        validationService.validFilmId(review.getFilmId());
        if (!reviewFromDb.getUserId().equals(review.getUserId())) {
            throw new IncorrectRequestBodyException("Отзыв с ID " + review.getReviewId() + ", написан пользователем с ID " +
                    reviewFromDb.getUserId() + ", пользователь с ID " + review.getUserId() + "не имеет права исправлять чужой отзыв");
        }
        if (!reviewFromDb.getFilmId().equals(review.getFilmId())) {
            throw new IncorrectRequestBodyException("Отзыв с ID " + review.getReviewId() + ", написан к фильму с ID " +
                    reviewFromDb.getFilmId() + ", а не к фильму с ID " + review.getFilmId());
        }
//        eventRepository.add(EventRepository.createEvent(getReviewById(review.getReviewId()).getUserId(),
//                EventType.REVIEW,
//                review.getReviewId(),
//                Operation.UPDATE));

        return reviewStorage.updateReview(review);
    }

    public boolean deleteReviewById(long id) {
        boolean isDelete = reviewStorage.deleteReviewById(id);
//        if (isDelete) {
//              eventRepository.add(EventRepository.createEvent(getReviewById(id).getUserId(),
//                EventType.REVIEW,
//                id,
//                Operation.REMOVE));
//        }
        return isDelete;
    }

    public Review getReviewById(long id) {
        return reviewStorage.getReviewById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Отзыва с ID " + id + " нет в базе"));
    }

    public List<Review> getAllReviewByFilmId(Long filmId, Integer count) {
        if (filmId != 0) {
            validationService.validFilmId(filmId);
        }
        return reviewStorage.getAllReviewByFilmId(filmId, count);
    }

    public void addLike(long reviewId, long userId) {
        validationService.validUserId(userId);
        Review receivedReview = getReviewById(reviewId);
        if (receivedReview.getUserId() == userId) {
            throw new IncorrectParameterException("Отзыв с ID " + reviewId + ", написан пользователем с ID " +
                    userId + ", нельзя оценивать свой отзыв");
        }
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        validationService.validUserId(userId);
        Review receivedReview = getReviewById(reviewId);
        if (receivedReview.getUserId() == userId) {
            throw new IncorrectParameterException("Отзыв с ID " + reviewId + ", написан пользователем с ID " +
                    userId + ", нельзя оценивать свой отзыв");
        }
        reviewStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(long reviewId, long userId) {
        validationService.validUserId(userId);
        Review receivedReview = getReviewById(reviewId);
        if (receivedReview.getUserId() == userId) {
            throw new IncorrectParameterException("Отзыв с ID " + reviewId + ", написан пользователем с ID " +
                    userId + ", нельзя оценивать свой отзыв");
        }
        reviewStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(long reviewId, long userId) {
        validationService.validUserId(userId);
        Review receivedReview = getReviewById(reviewId);
        if (receivedReview.getUserId() == userId) {
            throw new IncorrectParameterException("Отзыв с ID " + reviewId + ", написан пользователем с ID " +
                    userId + ", нельзя оценивать свой отзыв");
        }
        reviewStorage.deleteDislike(reviewId, userId);
    }
}
