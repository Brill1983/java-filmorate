package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaCategory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class GenreDbStorage implements GenreStorage{

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public List<Genre> findAllGenres() {
        String sql = "select * from GENRES";
        List<Genre> genreList = jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
        log.info("Найдено {} жанров", genreList.size());
        return genreList;
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        String sql = "select * from GENRES where GENRE_ID = :id";
        List<Genre> genreList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeGenre(rs));
        if(!genreList.isEmpty()) {
            log.info("Найдена жанр с ID: {} и названием {} ", genreList.get(0).getId(), genreList.get(0).getName());
            return Optional.of(genreList.get(0));
        } else {
            log.info("Жанр c идентификатором {} не найдена", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> findGenresByFilmId(int id) {
        String sql = "SELECT FG.GENRE_ID, G2.NAME FROM FILM_GENRES AS FG JOIN GENRES AS G2 on FG.GENRE_ID = G2.GENRE_ID WHERE FG.FILM_ID = :id";
        List<Genre> genreList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeGenre(rs));
        log.info("Для фильма {} найдено жанров {}", id, genreList.size());
        return genreList;
    }

    @Override
    public List<Integer> findGenresIdsByFilmId(int id) {
        String sql = "SELECT GENRE_ID FROM FILM_GENRES WHERE FILM_ID = :id";
        List<Integer> genreIdsList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeId(rs));
        log.info("Для фильма {} найдено {} ID жанров", id, genreIdsList.size());
        return genreIdsList;
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
    public void addGenresForFilm(Integer genreId, Long filmId) {
        String sql = "INSERT INTO FILM_GENRES(FILM_ID, GENRE_ID) VALUES ( :filmId, :genreId )";

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("filmId", filmId);
        map.addValue("genreId", genreId);

        int rowsNum = jdbcTemplate.update(sql, map);
        log.info("Добавлен жанр с ID: {} для фильма c ID {}", genreId, filmId);
    }


    // вспомогательные методы
    private Genre makeGenre(ResultSet rs) throws SQLException {
        Genre genre = new Genre(
                rs.getInt("GENRE_ID"),
                rs.getString("NAME")
        );
        return genre;
    }

    private Integer makeId(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("GENRE_ID");
        return id;
    }
}