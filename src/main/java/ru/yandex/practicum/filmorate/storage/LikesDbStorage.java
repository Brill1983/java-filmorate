package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class LikesDbStorage implements LikesStorage{

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public void userLikedFilm(long filmId, long userId) {
        String sql = "INSERT INTO LIKES(FILM_ID, USER_ID) VALUES ( :filmId, :userId )";

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("filmId", filmId);
        map.addValue("userId", userId);

        jdbcTemplate.update(sql, map);
        log.info("Фильму с ID: {} добавлен лайк от пользователя c ID {}", filmId, userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        String sql = "DELETE FROM LIKES WHERE FILM_ID = :filmId AND USER_ID = :userId";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("filmId", filmId);
        map.addValue("userId", userId);

        jdbcTemplate.update(sql, map);
        log.info("Удален лайка от пользователя с ID {} для фильма c ID {}", userId, filmId);
    }


    @Override
    public boolean checkUserLikedFilm (long filmId, long userId) {
        String sql = "SELECT USER_ID FROM LIKES WHERE FILM_ID = :id AND USER_ID = :userId";
        List<Long> likesList = jdbcTemplate.query(sql, Map.of("filmId", filmId, "userId", userId), (rs, rowNum) -> makeId(rs));
        log.info("Для фильма {} найдено {} лайков", filmId, likesList.size());
        if(likesList.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    private Long makeId(ResultSet rs) throws SQLException {
        Long id = rs.getLong("USER_ID");
        return id;
    }

}
