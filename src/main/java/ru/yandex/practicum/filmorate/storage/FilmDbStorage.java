package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import javax.print.attribute.standard.MediaSize;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage{

    private final NamedParameterJdbcOperations jdbcTemplate;
    private GenreStorage genreStorage;
    private LikesStorage likesStorage;

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO FILMS (NAME, RELEASE_DATE, DESCRIPTION, CATEGORY_MPA_ID, DURATION) " +
                "VALUES (:name, :releaseDate, :description, :categoryMpaId, :duration)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("name", film.getName());
        map.addValue("releaseDate", film.getReleaseDate());
        map.addValue("description", film.getDescription());
        map.addValue("categoryMpaId", film.getCategoryMpaId());
        map.addValue("duration", film.getDuration());

        jdbcTemplate.update(sql, map, keyHolder);
        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        for (Integer genreId : film.getGenres()) {
            genreStorage.addGenresForFilm(genreId, filmId);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        return null;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "select * from FILMS";
        List<Film> filmList = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        log.info("Найдено {} фильмов", filmList.size());
        return filmList;
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        String sql = "select * from FILMS WHERE FILM_ID = :id";
        List<Film> filmList = jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> makeFilm(rs));
        if(!filmList.isEmpty()) {
            log.info("Найдена фильм с ID: {} и названием {} ", filmList.get(0).getId(), filmList.get(0).getName());
            return Optional.of(filmList.get(0));
        } else {
            log.info("Фильм c идентификатором {} не найдена", id);
            return Optional.empty();
        }
    }


    // вспомогательные методы
    private Film makeFilm(ResultSet rs) throws SQLException { // long id, String name, String description, LocalDate releaseDate, int duration, int categoryMpaId
        int filmId = rs.getInt("FILM_ID");
        List<Integer> genresIdsList = genreStorage.findGenresIdsByFilmId(filmId);
        List<Long> likesList = likesStorage.getUsersIdLikedFilm(filmId);
        Set<Long> likesSet = new HashSet<>(likesList);
        Film film = new Film(
                filmId,
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                rs.getInt("CATEGORY_MPA_ID")
        );
        film.setGenres(genresIdsList);
        film.setLikes(likesSet);
        return film;
    }
}
