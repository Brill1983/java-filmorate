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
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MpaCategoryStorage mpaCategoryStorage;

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
        List<Integer> filmGenreIdList = film.getGenres().stream() // TODO - перенести в сервис
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());
        for (Integer genreId : filmGenreIdList) {
            genreStorage.addGenresForFilm(genreId, film.getId());
        }
        film.setGenres(genreStorage.findGenresByFilmId(filmId));
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

        List<Integer> filmGenreIdList = film.getGenres().stream()
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());
        genreStorage.deleteGenresOfFilm(film.getId());

        for (Integer genreId : filmGenreIdList) { // TODO - перенести в сервис
            genreStorage.addGenresForFilm(genreId, film.getId());
        }
        film.setGenres(genreStorage.findGenresByFilmId(film.getId()));

        film.setMpa(mpaCategoryStorage.findMpaCategoryById(film.getMpa().getId()).get()); // TODO - перенести в сервис
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, " +
                "F.DESCRIPTION, F.DURATION, F.RATE AS RT, F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID";

        List<Film> filmList = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs)); // TODO - добавить на слое сервис сборку жанров
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
        List<Film> filmList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeFilm(rs));

        if (!filmList.isEmpty()) {
            log.info("Найдена фильм с ID: {} и названием {} ", filmList.get(0).getId(), filmList.get(0).getName());
            List<Genre> filmGenres = new ArrayList<>();
            Map<Genre, List<Long>> filmsGenresMap = genreStorage.getFilmsGenresMap(); // TODO - перенести в сервис
            for (Genre genre : filmsGenresMap.keySet()) {
                if(filmsGenresMap.get(genre).contains(filmList.get(0).getId())) {
                    filmGenres.add(genre);
                }
            }
            filmGenres.sort(Comparator.comparing(Genre::getId));
            filmList.get(0).setGenres(filmGenres);
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
        List<Film> filmList = jdbcTemplate.query(sql, Map.of("count", count), (rs, rowNum) -> makeFilm(rs));
        log.info("По запросу на {} самых популярных фильмов сформиован список из {} фильмов", count, filmList.size());
        return filmList;
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        long filmId = rs.getLong("FILM_ID");

        Film film = new Film(
                filmId,
                rs.getString("FILM_NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                rs.getInt("RT"),
                new MpaCategory(rs.getInt("CATEGORY_MPA_ID"), rs.getString("MPA_NAME"))
        );
//        film.setGenres(genreStorage.findGenresByFilmId(filmId));
//        List<Genre> filmGenres = new ArrayList<>();
//        Map<Genre, List<Long>> filmsGenresMap = genreStorage.getFilmsGenresMap();
//        for (Genre genre : filmsGenresMap.keySet()) {
//            if(filmsGenresMap.get(genre).contains(filmId)) {
//                filmGenres.add(genre);
//            }
//        }
//        filmGenres.sort(Comparator.comparing(Genre::getId));
//        film.setGenres(filmGenres);
        return film;
    }
}
