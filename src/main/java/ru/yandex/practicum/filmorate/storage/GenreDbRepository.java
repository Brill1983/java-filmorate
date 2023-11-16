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

import java.util.*;

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
        while(rows.next()) {
            genresIdList.add(rows.getInt("GENRE_ID"));
        }
        log.info("Найдено {} ID жанров", genresIdList.size());
        return genresIdList;
    }

    // TODO clean
//        @Override
//    public List<Genre> findGenresByFilmId(long id) {
//        String sql = "SELECT FG.GENRE_ID, G2.NAME FROM FILM_GENRES AS FG " +
//                "JOIN GENRES AS G2 on FG.GENRE_ID = G2.GENRE_ID " +
//                "WHERE FG.FILM_ID = :id ORDER BY G2.GENRE_ID";
//        List<Genre> genreList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeGenre(rs));
//        log.info("Для фильма {} найдено жанров {}", id, genreList.size());
//        return genreList;
//    }

//    @Override
//    public boolean deleteGenresOfFilm(long id) {
//        String sql = "DELETE FROM FILM_GENRES WHERE FILM_ID = :id";
//        MapSqlParameterSource map = new MapSqlParameterSource();
//        map.addValue("id", id);
//
//        int count = jdbcTemplate.update(sql, map);
//        log.info("Удалено жанров {} для фильма c ID {}", count, id);
//        return count > 0;
//    }

//    @Override
//    public void addGenresForFilm(Integer genreId, Long filmId) {
//        String sql = "INSERT INTO FILM_GENRES(FILM_ID, GENRE_ID) VALUES ( :filmId, :genreId )";
//
//        MapSqlParameterSource map = new MapSqlParameterSource();
//        map.addValue("filmId", filmId);
//        map.addValue("genreId", genreId);
//
//        jdbcTemplate.update(sql, map);
//        log.info("Добавлен жанр с ID: {} для фильма c ID {}", genreId, filmId);
//    }


//    @Override
//    public Map<Genre, List<Long>> getFilmsGenresMap() { // TODO новый метод для вывода списка с 2 запросами к БД.
//        Map<Genre, List<Long>> filmGenreMap = new HashMap<>();
//
//        String sql = "SELECT FILM_ID, F.GENRE_ID AS GENRE_ID, G.NAME AS NAME " +
//                "FROM FILM_GENRES F " +
//                "LEFT JOIN GENRES G ON f.GENRE_ID = G.GENRE_ID " +
//                "ORDER BY GENRE_ID";
//
//        SqlRowSet rows = jdbcTemplate.getJdbcOperations().queryForRowSet(sql);
//
//        while(rows.next()) {
//            Genre genre = new Genre(rows.getInt("GENRE_ID"), rows.getString("NAME"));
//            if(filmGenreMap.containsKey(genre)) {
//                filmGenreMap.get(genre).add(rows.getLong("FILM_ID"));
//            } else {
//                List<Long> filmsIdsList = new ArrayList<>();
//                filmsIdsList.add(rows.getLong("FILM_ID"));
//                filmGenreMap.put(genre, filmsIdsList);
//            }
//        }
//        return filmGenreMap;
//    }

}
