package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.rowMapper.GenreRowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class GenreDbRepository implements GenreStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public List<Genre> findAllGenres() {
        String sql = "select * from GENRES";
        List<Genre> genreList = jdbcTemplate.query(sql, new GenreRowMapper());
        log.info("Найдено {} жанров", genreList.size());
        return genreList;
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        String sql = "select * from GENRES where GENRE_ID = :id";
        List<Genre> genreList = jdbcTemplate.query(sql, Map.of("id", id), new GenreRowMapper());
        if (!genreList.isEmpty()) {
            log.info("Найдена жанр с ID: {} и названием {} ", genreList.get(0).getId(), genreList.get(0).getName());
            return Optional.of(genreList.get(0));
        } else {
            log.info("Жанр c идентификатором {} не найдена", id);
            return Optional.empty();
        }
    }

    @Override
    public Genre createNewGenre(Genre genre) {
        String sql = "insert into GENRES (NAME) VALUES (:name)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("name", genre.getName());

        jdbcTemplate.update(sql, map, keyHolder);
        genre.setId(keyHolder.getKey().intValue());
        log.info("Внесен новый жанр {} c ID {}", genre.getName(), genre.getId());
        return genre;
    }

    @Override
    public List<Integer> findAllGenresIds() {
        String sql = "SELECT GENRE_ID FROM GENRES";
        SqlRowSet rows = jdbcTemplate.getJdbcOperations().queryForRowSet(sql);
        List<Integer> genresIdList = new ArrayList<>();
        while (rows.next()) {
            genresIdList.add(rows.getInt("GENRE_ID"));
        }
        log.info("Найдено {} ID жанров", genresIdList.size());
        return genresIdList;
    }
}
