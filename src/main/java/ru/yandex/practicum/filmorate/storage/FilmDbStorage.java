package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaCategory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public Film addFilm(Film film) {

        String sql = "INSERT INTO FILMS (NAME, RELEASE_DATE, DESCRIPTION, CATEGORY_MPA_ID, DURATION, RATE) " +
                "VALUES (:name, :releaseDate, :description, :categoryMpaId, :duration, :rate)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("name", film.getName());
        map.addValue("releaseDate", film.getReleaseDate());
        map.addValue("description", film.getDescription());
        map.addValue("categoryMpaId", film.getMpa().getId());
        map.addValue("duration", film.getDuration());
        map.addValue("rate", film.getRate());

        jdbcTemplate.update(sql, map, keyHolder);
        Long filmId = keyHolder.getKey().longValue();

        film.setId(filmId);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE FILMS SET NAME = :name, RELEASE_DATE = :releaseDate, DESCRIPTION = :description, " +
                "CATEGORY_MPA_ID = :categoryMpaId, DURATION = :duration, RATE = :rate " +
                "WHERE FILM_ID = :id";
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("id", film.getId());
        map.addValue("name", film.getName());
        map.addValue("releaseDate", film.getReleaseDate());
        map.addValue("description", film.getDescription());
        map.addValue("categoryMpaId", film.getMpa().getId());
        map.addValue("duration", film.getDuration());
        map.addValue("rate", film.getRate());

        jdbcTemplate.update(sql, map);

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, " +
                "F.DESCRIPTION, F.DURATION, F.RATE AS RT, F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID";

        Map<Genre, List<Long>> filmsGenresMap = getFilmsGenresMap();
        List<Film> filmList = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs, filmsGenresMap));
        log.info("Найдено {} фильмов", filmList.size());
        return filmList;
    }

    @Override
    public Optional<Film> getFilmById(long id) {

        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE AS RT, " +
                "F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "WHERE F.FILM_ID = :id";

        Map<Genre, List<Long>> filmsGenresMap = getFilmsGenresMap();
        List<Film> filmList = jdbcTemplate.query(sql, Map.of("id", id),
                (rs, rowNum) -> makeFilm(rs, filmsGenresMap));

        if (!filmList.isEmpty()) {
            log.info("Найден фильм с ID: {} и названием {} ", filmList.get(0).getId(), filmList.get(0).getName());
            return Optional.of(filmList.get(0));
        } else {
            log.info("Фильм c идентификатором {} не найден в БД", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getPopularFilmList(int count) {
        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.CATEGORY_MPA_ID, " +
                "M.NAME AS MPA_NAME, COUNT(L.USER_ID) + F.RATE AS RT " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY RT DESC " +
                "LIMIT :count";
        Map<Genre, List<Long>> filmsGenresMap = getFilmsGenresMap();
        List<Film> filmList = jdbcTemplate.query(sql, Map.of("count", count),
                (rs, rowNum) -> makeFilm(rs, filmsGenresMap));
        log.info("По запросу на {} самых популярных фильмов сформиован список из {} фильмов", count, filmList.size());
        return filmList;
    }

    private Film makeFilm(ResultSet rs, Map<Genre, List<Long>> filmsGenresMap) throws SQLException {
        long filmId = rs.getLong("FILM_ID");

        Set<Genre> filmGenres = new HashSet<>();
        for (Genre genre : filmsGenresMap.keySet()) { // итерация по мапе, где ключ: жанр, значение: список с ID фильмов.
            if (filmsGenresMap.get(genre).contains(filmId)) { // если ID фильма содержится в списке-значении мапы - то формируем список жанров для фильма
                filmGenres.add(genre);
            }
        }
        // TODO добавить сборку лайков и режиссеров
        return new Film(
                filmId,
                rs.getString("FILM_NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                rs.getInt("RT"),
                new MpaCategory(rs.getInt("CATEGORY_MPA_ID"), rs.getString("MPA_NAME")),
                filmGenres
                );
    }

    private Map<Genre, List<Long>> getFilmsGenresMap() {
        Map<Genre, List<Long>> filmGenreMap = new HashMap<>();

        String sql = "SELECT FILM_ID, F.GENRE_ID AS GENRE_ID, G.NAME AS NAME " +
                "FROM FILM_GENRES F " +
                "LEFT JOIN GENRES G ON f.GENRE_ID = G.GENRE_ID " +
                "ORDER BY NAME"; // TODO проверить, что список сортированный

        SqlRowSet rows = jdbcTemplate.getJdbcOperations().queryForRowSet(sql);

        while(rows.next()) {
            Genre genre = new Genre(rows.getInt("GENRE_ID"), rows.getString("NAME"));
            if(filmGenreMap.containsKey(genre)) {
                filmGenreMap.get(genre).add(rows.getLong("FILM_ID"));
            } else {
                List<Long> filmsIdsList = new ArrayList<>();
                filmsIdsList.add(rows.getLong("FILM_ID"));
                filmGenreMap.put(genre, filmsIdsList);
            }
        }
        return filmGenreMap;
    }
}
