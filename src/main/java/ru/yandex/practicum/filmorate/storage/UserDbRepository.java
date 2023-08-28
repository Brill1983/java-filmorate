package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.rowMapper.UserRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class UserDbRepository implements UserStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public Optional<User> getUserById(long id) {
        String sql = "SELECT * FROM USERS WHERE USER_ID = :id";
        List<User> userList = jdbcTemplate.query(sql, Map.of("id", id), new UserRowMapper());
        if (!userList.isEmpty()) {
            log.info("Найден пользователь с ID: {} и именем {} ", userList.get(0).getId(), userList.get(0).getName());
            return Optional.of(userList.get(0));
        } else {
            log.info("Пользователь c идентификатором {} не найден в БД", id);
            return Optional.empty();
        }
    }

    @Override
    public User saveUser(User user) {
        String sql = "INSERT INTO USERS (EMAIL, NAME, LOGIN, BIRTHDAY) " +
                "VALUES (:email, :name, :login, :birthday)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("email", user.getEmail());
        map.addValue("name", user.getName());
        map.addValue("login", user.getLogin());
        map.addValue("birthday", user.getBirthday());

        jdbcTemplate.update(sql, map, keyHolder);
        Long userId = keyHolder.getKey().longValue();
        user.setId(userId);

        return user;
    }

    @Override
    public boolean delete(long userId) {
        String sql = "DELETE FROM FRIENDS WHERE USER_ID = :userId OR FRIEND_ID = :userId";
        jdbcTemplate.update(sql, Map.of("userId", userId));
        sql = "DELETE FROM REVIEWS_LIKES WHERE USER_ID = :userId";
        jdbcTemplate.update(sql, Map.of("userId", userId));
        sql = "DELETE FROM REVIEWS WHERE USER_ID = :userId";
        jdbcTemplate.update(sql, Map.of("userId", userId));
        sql = "DELETE FROM EVENTS WHERE USER_ID = :userId";
        jdbcTemplate.update(sql, Map.of("userId", userId));
        sql = "DELETE FROM LIKES WHERE USER_ID = :userId";
        jdbcTemplate.update(sql, Map.of("userId", userId));
        sql = "DELETE FROM USERS WHERE USER_ID = :userId";
        int count = jdbcTemplate.update(sql, Map.of("userId", userId));
        log.info("Удален пользователь с идентификатором {}", userId);
        return count > 0;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE USERS SET EMAIL = :email, NAME = :name, LOGIN = :login, " +
                "BIRTHDAY = :birthday WHERE USER_ID = :id";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("id", user.getId());
        map.addValue("email", user.getEmail());
        map.addValue("name", user.getName());
        map.addValue("login", user.getLogin());
        map.addValue("birthday", user.getBirthday());

        jdbcTemplate.update(sql, map);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM USERS";
        List<User> usersList = jdbcTemplate.query(sql, new UserRowMapper());
        log.info("Найдено {} пользователей", usersList.size());
        return usersList;
    }
}
