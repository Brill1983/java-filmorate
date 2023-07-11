package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

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
    public List<Long> getUsersIdLikedFilm(int id) {
        String sql = "SELECT USER_ID FROM LIKES WHERE FILM_ID = :id";
        List<Long> likesList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeId(rs));
        log.info("Для фильма {} найдено {} лайков", id, likesList.size());
        return likesList;
    }

    private Long makeId(ResultSet rs) throws SQLException {
        Long id = rs.getLong("USER_ID");
        return id;
    }

}
