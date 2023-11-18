package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewStorage reviewRepository;
    private final EventStorage eventRepository;
    private final ValidationService validationService;

    public Review addReview(Review review) {
        validationService.validUserId(review.getUserId());
        validationService.validFilmId(review.getFilmId());
        Review reviewFromDb = reviewRepository.addReview(review);
        eventRepository.add(new Event(review.getUserId(), EventType.REVIEW, reviewFromDb.getReviewId(), Operation.ADD));
        return reviewFromDb;
    }

    public Review updateReview(Review review) {
        Review reviewFromDb = getReviewById(review.getReviewId());
        validationService.validUserId(review.getUserId());
        validationService.validFilmId(review.getFilmId());
        eventRepository.add(new Event(reviewFromDb.getUserId(), EventType.REVIEW, reviewFromDb.getReviewId(), Operation.UPDATE));
        return reviewRepository.updateReview(review);
    }

    public boolean deleteReviewById(long id) {
        Review review = getReviewById(id);
        boolean isDelete = reviewRepository.deleteReviewById(id);
        if (isDelete) {
            eventRepository.add(new Event(review.getUserId(), EventType.REVIEW, id, Operation.REMOVE));
        }
        return isDelete;
    }

    public Review getReviewById(long id) {
        return reviewRepository.getReviewById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Отзыва с ID " + id + " нет в базе"));
    }

    public List<Review> getAllReviewByFilmId(Long filmId, Integer count) {
        if (filmId != 0) {
            validationService.validFilmId(filmId);
        }
        return reviewRepository.getAllReviewByFilmId(filmId, count);
    }

    public void addLike(long reviewId, long userId) {
        validationService.validReviewAuthor(reviewId, userId);
        reviewRepository.addLike(reviewId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        validationService.validReviewAuthor(reviewId, userId);
        reviewRepository.addDislike(reviewId, userId);
    }

    public void deleteLike(long reviewId, long userId) {
        validationService.validReviewAuthor(reviewId, userId);
        reviewRepository.deleteLike(reviewId, userId);
    }

    public void deleteDislike(long reviewId, long userId) {
        validationService.validReviewAuthor(reviewId, userId);
        reviewRepository.deleteDislike(reviewId, userId);
    }
}
