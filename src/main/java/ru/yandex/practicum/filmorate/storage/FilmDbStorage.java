package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaCategory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;
    private final GenreStorage genreStorage;

    @Override
    public Film addFilm(Film film) {

        String sql = "INSERT INTO FILMS (NAME, RELEASE_DATE, DESCRIPTION, CATEGORY_MPA_ID, DURATION) " +
                "VALUES (:name, :releaseDate, :description, :categoryMpaId, :duration)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("name", film.getName());
        map.addValue("releaseDate", film.getReleaseDate());
        map.addValue("description", film.getDescription());
        map.addValue("categoryMpaId", film.getCategoryMpa().getId());
        map.addValue("duration", film.getDuration());

        jdbcTemplate.update(sql, map, keyHolder);
        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        for (Genre genre : film.getGenres()) {
            genreStorage.addGenresForFilm(genre.getId(), filmId);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE FILMS SET NAME = :name, RELEASE_DATE = :releaseDate, DESCRIPTION = :description, " +
                "CATEGORY_MPA_ID = :categoryMpaId, DURATION = :duration WHERE FILM_ID = :id";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("id", film.getId());
        map.addValue("name", film.getName());
        map.addValue("releaseDate", film.getReleaseDate());
        map.addValue("description", film.getDescription());
        map.addValue("categoryMpaId", film.getCategoryMpa().getId());
        map.addValue("duration", film.getDuration());

        jdbcTemplate.update(sql, map);
        genreStorage.deleteGenresOfFilm(film.getId());
        for (Genre genre : film.getGenres()) {
            genreStorage.addGenresForFilm(genre.getId(), film.getId());
        }
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, " +
                "F.DESCRIPTION, F.DURATION, F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID";
        List<Film> filmList = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        log.info("Найдено {} фильмов", filmList.size());
        return filmList;
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, " +
                "F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME FROM FILMS AS F JOIN MPA_CATEGORIES AS M " +
                "ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID WHERE F.FILM_ID = :id";
        List<Film> filmList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeFilm(rs));
        if (!filmList.isEmpty()) {
            log.info("Найдена фильм с ID: {} и названием {} ", filmList.get(0).getId(), filmList.get(0).getName());
            return Optional.of(filmList.get(0));
        } else {
            log.info("Фильм c идентификатором {} не найден в БД", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getPopularFilmList(int count) {
        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, " +
                "F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME, COUNT(L.USER_ID) " +
                "FROM FILMS AS F JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID" +
                "JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID" +
                "GROUP BY F.FILM_ID" +
                "ORDER BY COUNT(L.USER_ID) DESC" +
                "LIMIT :count";
        List<Film> filmList = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        log.info("По запросу на {} самых популярных фильмов сформиован список из {} фильмов", count, filmList.size());
        return filmList;
    }


    // вспомогательные методы
    private Film makeFilm(ResultSet rs) throws SQLException {
        long filmId = rs.getLong("FILM_ID");

        List<Genre> genresIdsList = genreStorage.findGenresByFilmId(filmId);

        Film film = new Film(
                filmId,
                rs.getString("FILM_NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                new MpaCategory(rs.getInt("CATEGORY_MPA_ID"), rs.getString("MPA_NAME"))
        );
        film.setGenres(genresIdsList);
        return film;
    }
}
