package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.LikeAlreadyExistException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.rowMapper.ReviewRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class ReviewDbRepository implements ReviewStorage {

    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public Review addReview(Review review) {
        String sql = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID) " +
                "VALUES (:content, :isPositive, :userId, :filmId)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("content", review.getContent());
        map.addValue("isPositive", review.getIsPositive());
        map.addValue("userId", review.getUserId());
        map.addValue("filmId", review.getFilmId());
        jdbcOperations.update(sql, map, keyHolder);
        review.setReviewId(keyHolder.getKey().longValue());
        log.info("В базу занесен отзыв с идентификатором {}", review.getReviewId());
        return getReviewById(review.getReviewId()).get();
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE REVIEWS SET CONTENT = :content, IS_POSITIVE = :isPositive " +
                "WHERE REVIEW_ID = :reviewId ";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("content", review.getContent());
        map.addValue("isPositive", review.getIsPositive());
        map.addValue("reviewId", review.getReviewId());

        jdbcOperations.update(sql, map);
        log.info("Обновлен отзыв с идентификатором {}", review.getReviewId());
        return getReviewById(review.getReviewId()).get(); //TODO переделать под проверку на наличие через count
    }

    @Override
    public boolean deleteReviewById(long id) {
        String sql = "DELETE FROM REVIEWS " +
                "WHERE REVIEW_ID = :reviewId ";
        int count = jdbcOperations.update(sql, Map.of("reviewId", id));
        if (count > 0) {
            log.info("Удален отзыв с идентификатором {}", id);
        }
        return count > 0;
    }

    @Override
    public Optional<Review> getReviewById(long id) {
        String sql = "SELECT * FROM REVIEWS WHERE REVIEW_ID = :reviewId";
        List<Review> reviews = jdbcOperations.query(sql, Map.of("reviewId", id), new ReviewRowMapper());

        if (!reviews.isEmpty()) {
            log.info("Найден отзыв с ID: {} к фильму {} от пользователя {}", reviews.get(0).getReviewId(), reviews.get(0).getFilmId(), reviews.get(0).getUserId());
            return Optional.of(reviews.get(0));
        } else {
            log.info("Отзыв c идентификатором {} не найден в БД", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getAllReviewByFilmId(Long filmId, int count) {
        String sql;
        if (filmId == 0) {
            sql = "SELECT * " +
                    "FROM REVIEWS " +
                    "ORDER BY USEFUL DESC ";
        } else {
            sql = "SELECT * " +
                    "FROM REVIEWS " +
                    "WHERE FILM_ID = :filmId " +
                    "ORDER BY USEFUL DESC " +
                    "LIMIT :count ";
        }
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("filmId", filmId);
        map.addValue("count", count);
        List<Review> reviews = jdbcOperations.query(sql, map, new ReviewRowMapper());
        log.info("Количнсвто отзывов в списке {}", reviews.size());
        return reviews;
    }

    @Override
    public void addLike(long reviewId, long userId) {
        Boolean isLike = true;
        String sqlReviewUserLikes = "SELECT * " + // запрашиваем лайки от пользователя для отзыва
                "FROM REVIEWS_LIKES " +
                "WHERE REVIEW_ID = :reviewId AND USER_ID = :userId ";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("reviewId", reviewId);
        map.addValue("userId", userId);
        SqlRowSet reviewLikeRows = jdbcOperations.queryForRowSet(sqlReviewUserLikes, map);
        if (!reviewLikeRows.next()) {
            String sql = "INSERT INTO REVIEWS_LIKES (REVIEW_ID, USER_ID, IS_LIKE) " +
                    "VALUES (:reviewId, :userId, :isLike) ";
            MapSqlParameterSource mapForSetLike = new MapSqlParameterSource();
            mapForSetLike.addValue("reviewId", reviewId);
            mapForSetLike.addValue("userId", userId);
            mapForSetLike.addValue("isLike", isLike);
            jdbcOperations.update(sql, mapForSetLike);
            log.info("Добавлен лайк к отзыву {} от пользователя {}", reviewId, userId);
            incrementUseful(reviewId);
        } else if (reviewLikeRows.next() && !reviewLikeRows.getBoolean("ISLIKE")) {
            String sql = "UPDATE REVIEWS_LIKES " +
                    "SET IS_LIKE = :isLike " +
                    "WHERE REVIEW_ID = :reviewId AND USER_ID = :userId";
            MapSqlParameterSource mapForUpdateLike = new MapSqlParameterSource();
            mapForUpdateLike.addValue("reviewId", reviewId);
            mapForUpdateLike.addValue("userId", userId);
            mapForUpdateLike.addValue("isLike", isLike);
            jdbcOperations.update(sql, mapForUpdateLike);
            log.info("Лайк к отзыву {} от пользователя {} изменен на дизлайк", reviewId, userId);
            incrementUseful(reviewId);
        } else {
            throw new LikeAlreadyExistException("Данный пользователь уже ставил лайк");
        }
    }

    @Override
    public void addDislike(long reviewId, long userId) { // TODO можно ли слить в один метод с лайками
        Boolean isLike = false;
        String sqlReviewUserLikes = "SELECT * " +
                "FROM REVIEWS_LIKES " +
                "WHERE REVIEW_ID = :reviewId AND USER_ID = :userId ";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("reviewId", reviewId);
        map.addValue("userId", userId);
        SqlRowSet reviewLikeRows = jdbcOperations.queryForRowSet(sqlReviewUserLikes, map);
        if (!reviewLikeRows.next()) {
            String sql = "INSERT INTO REVIEWS_LIKES (REVIEW_ID, USER_ID, IS_LIKE) " +
                    "VALUES (:reviewId, :userId, :isLike) ";
            MapSqlParameterSource mapForSetDisLike = new MapSqlParameterSource();
            mapForSetDisLike.addValue("reviewId", reviewId);
            mapForSetDisLike.addValue("userId", userId);
            mapForSetDisLike.addValue("isLike", isLike);
            jdbcOperations.update(sql, mapForSetDisLike);
            log.info("Добавлен дизлайк к отзыву {} от пользователя {}", reviewId, userId);
            decrementUseful(reviewId);
        } else if (reviewLikeRows.next() && reviewLikeRows.getBoolean("ISLIKE")) {
            String sql = "UPDATE REVIEWS_LIKES " +
                    "SET IS_LIKE = :isLike " +
                    "WHERE REVIEW_ID = :reviewId AND USER_ID = :userId";
            MapSqlParameterSource mapForUpdateLike = new MapSqlParameterSource();
            mapForUpdateLike.addValue("reviewId", reviewId);
            mapForUpdateLike.addValue("userId", userId);
            mapForUpdateLike.addValue("isLike", isLike);
            jdbcOperations.update(sql, mapForUpdateLike);
            log.info("Дизлайк к отзыву {} от пользователя {} изменен на лайк", reviewId, userId);
            decrementUseful(reviewId);
        } else {
            throw new LikeAlreadyExistException("Данный пользователь уже ставил дизлайк");
        }
    }

    @Override
    public void deleteLike(long reviewId, long userId) {
        Boolean isLike = true;
        String sql = "DELETE FROM REVIEWS_LIKES " +
                "WHERE REVIEW_ID = :reviewId AND USER_ID = :userId AND IS_LIKE = :isLike";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("reviewId", reviewId);
        map.addValue("userId", userId);
        map.addValue("isLike", isLike);
        int count = jdbcOperations.update(sql, map);
        if (count > 0) {
            decrementUseful(reviewId);
        }
    }

    @Override
    public void deleteDislike(long reviewId, long userId) {
        Boolean isLike = false;
        String sql = "DELETE FROM REVIEWS_LIKES " +
                "WHERE REVIEW_ID = :reviewId AND USER_ID = :userId AND IS_LIKE = :isLike";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("reviewId", reviewId);
        map.addValue("userId", userId);
        map.addValue("isLike", isLike);
        int count = jdbcOperations.update(sql, map);
        if (count > 0) {
            incrementUseful(reviewId);
        }
    }

    private void incrementUseful(long reviewId) {
        String sqlForReviews = "UPDATE REVIEWS SET USEFUL = USEFUL + 1 " +
                "WHERE REVIEW_ID = :reviewId ";
        jdbcOperations.update(sqlForReviews, Map.of("reviewId", reviewId));
        log.info("Полезность отзыва {} повышена", reviewId);
    }

    private void decrementUseful(long reviewId) {
        String sqlForReviews = "UPDATE REVIEWS SET USEFUL = USEFUL - 1 " +
                "WHERE REVIEW_ID = :reviewId ";
        jdbcOperations.update(sqlForReviews, Map.of("reviewId", reviewId));
        log.info("Полезность отзыва {} понижена", reviewId);
    }
}
