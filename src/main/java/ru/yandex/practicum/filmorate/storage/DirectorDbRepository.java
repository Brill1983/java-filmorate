package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.rowMapper.DirectorRowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class DirectorDbRepository implements DirectorStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM DIRECTORS";
        List<Director> directorList = jdbcTemplate.query(sql, new DirectorRowMapper());
        log.info("Найдено в базе {} режиссеров", directorList.size());
        return directorList;
    }

    @Override
    public List<Integer> findAllDirectorsIds() {
        String sql = "SELECT DIRECTOR_ID FROM DIRECTORS";
        SqlRowSet rows = jdbcTemplate.getJdbcOperations().queryForRowSet(sql);
        List<Integer> dirIdList = new ArrayList<>();
        while (rows.next()) {
            dirIdList.add(rows.getInt("DIRECTOR_ID"));
        }
        log.info("Найдено в базе {} ID режиссеров", dirIdList.size());
        return dirIdList;
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        String sql = "SELECT * FROM DIRECTORS WHERE DIRECTOR_ID = :directorId";
        List<Director> directors = jdbcTemplate.query(sql, Map.of("directorId", id), new DirectorRowMapper());
        if (!directors.isEmpty()) {
            log.info("Найден в базе режиссер с ID: {} и именем {} ", directors.get(0).getId(), directors.get(0).getName());
            return Optional.of(directors.get(0));
        } else {
            log.info("Режиссер c идентификатором {} не найден в БД", id);
            return Optional.empty();
        }
    }

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO DIRECTORS (NAME) VALUES (:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("name", director.getName());
        jdbcTemplate.update(sql, map, keyHolder);
        director.setId(keyHolder.getKey().intValue());
        log.info("Внесен новый режиссер {} c ID {}", director.getName(), director.getId());
        return getDirectorById(director.getId()).get();
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE DIRECTORS SET NAME = :name WHERE DIRECTOR_ID = :id";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("id", director.getId());
        map.addValue("name", director.getName());
        jdbcTemplate.update(sql, map);
        log.info("Режиссер ID {} изменен на {}", director.getId(), director.getName());
        return getDirectorById(director.getId()).get();
    }

    @Override
    public void deleteDirector(int directorId) {
        String sql = "DELETE FROM FILM_DIRECTORS WHERE DIRECTOR_ID = :directorId";
        jdbcTemplate.update(sql, Map.of("directorId", directorId));
        sql = "DELETE FROM DIRECTORS WHERE DIRECTOR_ID = :directorId";
        jdbcTemplate.update(sql, Map.of("directorId", directorId));
        log.info("Режиссер с иденттификатором {} удален", directorId);
    }
}
