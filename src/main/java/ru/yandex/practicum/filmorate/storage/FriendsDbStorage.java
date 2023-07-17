package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class FriendsDbStorage implements FriendsStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public void addAsFriend(long userId, long friendId) {
        String sql = "INSERT INTO FRIENDS( USER_ID, FRIEND_ID ) VALUES ( :userId, :friendId )";

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("userId", userId);
        map.addValue("friendId", friendId);

        int count = jdbcTemplate.update(sql, map);
        System.out.println(count);
        log.info("Пользователь с ID {} добавил в свои друзья пользователя c ID {}", userId, friendId);
    }

    @Override
    public boolean deleteFriend(long userId, long friendId) {
        String sql = "DELETE FROM FRIENDS WHERE USER_ID = :userId AND FRIEND_ID = :friendId";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("userId", userId);
        map.addValue("friendId", friendId);

        int count = jdbcTemplate.update(sql, map);
        if (count > 0) {
            log.info("Пользователь с ID {} удалил из свои друзей пользователя c ID {}", userId, friendId);
        } else {
            log.info("У пользователя с ID {} в друзьях нет пользователя c ID {}, удалять некого", userId, friendId);
        }
        return count > 0;
    }

    @Override
    public List<User> getFriendsList(long userId) {
        String sql = "SELECT U.* FROM FRIENDS AS F JOIN USERS AS U ON F.FRIEND_ID = U.USER_ID WHERE F.USER_ID = :id";
        List<User> usersList = jdbcTemplate.query(sql, Map.of("id", userId), (rs, rowNum) -> makeUser(rs));
        log.info("У пользователя {} найдено {} друзей", userId, usersList.size());
        return usersList;
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        String sql = "SELECT * FROM USERS WHERE USER_ID IN (" +
                "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = :userId " +
                "INTERSECT SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = :otherId)";
        List<User> usersList = jdbcTemplate.query(sql, Map.of("userId", userId, "otherId", otherId), (rs, rowNum) -> makeUser(rs));
        log.info("У пользователя {} и пользователя {} найдено {} общих друзей", userId, otherId, usersList.size());
        return usersList;
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("USER_ID"),
                rs.getString("EMAIL"),
                rs.getString("LOGIN"),
                rs.getString("NAME"),
                rs.getDate("BIRTHDAY").toLocalDate()
        );
    }
}

