package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.rowMapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.rowMapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.rowMapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.rowMapper.UserRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class FilmDbRepository implements FilmStorage {

    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public Optional<Film> getFilmById(long filmId) {

        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE AS RT, " +
                "F.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "WHERE F.FILM_ID = :filmId";

        List<Film> filmList = jdbcOperations.query(sql, Map.of("filmId", filmId), new FilmRowMapper());

        if (!filmList.isEmpty()) {
            log.info("Найден фильм с ID: {} и названием {} ", filmList.get(0).getId(), filmList.get(0).getName());
            Film film = filmList.get(0);
            film.setGenres(new HashSet<>(getFilmGenres(filmId)));
            film.setDirectors(getDirectorListByFilmId(filmId));
            film.setLikes(getLikesByFilmId(filmId));
            return Optional.of(film);
        } else {
            log.info("Фильм c идентификатором {} не найден в БД", filmId);
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getFilmsList() {
        String sql = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, " +
                "F.DESCRIPTION, F.DURATION, F.RATE AS RT, F.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID";

        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
        List<Film> filmList = jdbcOperations.query(sql, (rs, rowNum) -> makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));
        log.info("Найдено {} фильмов", filmList.size());
        return filmList;
    }

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
        jdbcOperations.update(sql, map, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        film.getGenres()
                .forEach(genre -> addGenresForFilm(genre.getId(), film.getId()));
        film.getDirectors()
                .forEach(director -> addDirectorToFilm(film.getId(), director.getId()));
        log.info("В базу занесен фильм с идентификатором {}", film.getId());
        return getFilmById(film.getId()).get();
    }

    @Override
    public boolean delete(long filmId) { //TODO попробовать delete on cascade, дописать удаление отзыва.

        String sql = "DELETE FROM FILM_GENRES WHERE FILM_ID = :filmId";
        jdbcOperations.update(sql, Map.of("filmId", filmId));
        sql = "DELETE FROM LIKES WHERE FILM_ID = :filmId";
        jdbcOperations.update(sql, Map.of("filmId", filmId));
        //TODO добавить удаление ревью
        sql = "DELETE FROM FILM_DIRECTORS WHERE FILM_ID = :filmId";
        jdbcOperations.update(sql, Map.of("filmId", filmId));
        sql = "DELETE FROM REVIEWS WHERE FILM_ID = :filmId";
        jdbcOperations.update(sql, Map.of("filmId", filmId));
        sql = "DELETE FROM FILMS WHERE FILM_ID = :filmId";
        int count = jdbcOperations.update(sql, Map.of("filmId", filmId));
        log.info("Удален фильм с идентификатором {}", filmId);
        return count > 0;
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

        jdbcOperations.update(sql, map);
        deleteGenresOfFilm(film.getId());
        film.getGenres()
                .forEach(genre -> addGenresForFilm(genre.getId(), film.getId()));

        deleteDirectorsOfFilm(film.getId());
        film.getDirectors()
                .forEach(director -> addDirectorToFilm(film.getId(), director.getId()));
        log.info("Обновлен фильм с идентификатором {}", film.getId());
        return getFilmById(film.getId()).get();
    }

    @Override
    public List<Genre> getFilmGenres(long filmId) {
        String sql = "SELECT FG.GENRE_ID, G2.NAME FROM FILM_GENRES AS FG " +
                "JOIN GENRES AS G2 on FG.GENRE_ID = G2.GENRE_ID " +
                "WHERE FG.FILM_ID = :filmId ORDER BY G2.GENRE_ID";
        List<Genre> genres = jdbcOperations.query(sql, Map.of("filmId", filmId), new GenreRowMapper());
        log.info("Для фильма {} найдено жанров {}", filmId, genres.size());
        return genres;
    }

    @Override
    public List<Film> getDirectorFilmListByYear(int directorId) {

        final String sql = "SELECT * " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID=M.CATEGORY_MPA_ID " +
                "JOIN FILM_DIRECTORS FD ON F.FILM_ID = FD.FILM_ID " +
                "JOIN DIRECTORS D ON FD.DIRECTOR_ID = D.DIRECTOR_ID " +
                "WHERE D.DIRECTOR_ID = :directorId " +
                "ORDER BY RELEASE_DATE";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("directorId", directorId), (rs, rowNum) ->
//                makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, Map.of("directorId", directorId), new FilmRowMapper()));

        log.info("Для режиссера {} найдено {} фильмов отсортированных по году выпуска", directorId, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getDirectorFilmListByLikes(int directorId) {

        final String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATE, M.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "JOIN FILM_DIRECTORS FD ON F.FILM_ID = FD.FILM_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE FD.DIRECTOR_ID = :directorId " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(L.USER_ID) + F.RATE DESC";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("directorId", directorId), (rs, rowNum) ->
//                makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, Map.of("directorId", directorId), new FilmRowMapper()));

        log.info("Для режиссера {} найдено {} фильмов отсортированных по по популярности", directorId, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> searchFilmsByDirAndName(String query) {
        String regex = "%" + query + "%";
        String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATE, M.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE UPPER(F.NAME) LIKE UPPER(:regex) OR F.FILM_ID IN (" +
                "SELECT FD.FILM_ID " +
                "FROM FILM_DIRECTORS FD " +
                "LEFT JOIN DIRECTORS D ON D.DIRECTOR_ID = FD.DIRECTOR_ID " +
                "WHERE UPPER(D.NAME) LIKE UPPER(:regex)" +
                ") " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(L.USER_ID) DESC";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("regex", regex), (rs, rowNum) ->
//                makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, Map.of("regex", regex), new FilmRowMapper()));

        log.info("Поиск по именам режиссеров и названиям дал {} резльтатов", filmList.size());
        return filmList;
    }

    @Override
    public List<Film> searchFilmsByName(String query) {
        String regex = "%" + query + "%";
        String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATE, M.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID " +
                "WHERE UPPER(F.NAME) LIKE UPPER(:regex) " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(L.USER_ID) DESC";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("regex", regex), (rs, rowNum) ->
//                makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, Map.of("regex", regex), new FilmRowMapper()));

        log.info("Поиск по названиям дал {} резльтатов", filmList.size());
        return filmList;
    }

    @Override
    public List<Film> searchFilmsByDir(String query) {
        String regex = "%" + query + "%";
        String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATE, M.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE F.FILM_ID IN (" +
                "SELECT FD.FILM_ID " +
                "FROM FILM_DIRECTORS FD " +
                "LEFT JOIN DIRECTORS D ON D.DIRECTOR_ID = FD.DIRECTOR_ID " +
                "WHERE UPPER(D.NAME) LIKE UPPER(:regex)" +
                ") " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(L.USER_ID) DESC";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("regex", regex), (rs, rowNum) ->
//                makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, Map.of("regex", regex), new FilmRowMapper()));

        log.info("Поиск по именам режиссеров дал {} резльтатов", filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilmsByYearAndGenre(int genreId, int year, int count) {
        String sql = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE, M.CATEGORY_MPA_ID, M.NAME, " +
//                "COUNT(L.USER_ID) " +
                "COUNT(L.USER_ID) + F.RATE AS RT " +
                "FROM FILMS as F " +
                "JOIN MPA_CATEGORIES as M on F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE F.FILM_ID IN (" +
                "SELECT FILM_ID " +
                "FROM FILM_GENRES " +
                "WHERE GENRE_ID = :genreId) " +
                "AND EXTRACT(YEAR FROM F.RELEASE_DATE) = :year " +
                "GROUP BY F.FILM_ID " +
//                "order by COUNT(L.USER_ID) " +
                "ORDER BY RT DESC " +
                "LIMIT :count";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("genreId", genreId, "year", year, "count", count),
//                (rs, rowNum) -> makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("genreId", genreId);
        map.addValue("year", year);
        map.addValue("count", count);
        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, map, new FilmRowMapper()));

        log.info("Поиск ТОП {} фильмов в жанре c ID {} за {} год дал {} резльтатов", count, genreId, year, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilmsByYear(int year, int count) {
        final String sql = "SELECT F.FILM_ID, " +
                "F.NAME, " +
                "F.RELEASE_DATE, " +
                "F.DESCRIPTION, " +
                "F.DURATION, F.RATE, " +
                "M.CATEGORY_MPA_ID, " +
                "M.NAME, " +
//                "COUNT(L.USER_ID) " +
                "COUNT(L.USER_ID) + F.RATE AS RT " +
                "from FILMS as F " +
                "join MPA_CATEGORIES as M on F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "where extract(YEAR from F.RELEASE_DATE) = :year " +
                "group by F.FILM_ID " +
//                "order by COUNT(L.USER_ID) " +
                "ORDER BY RT DESC " +
                "limit :count";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("year", year, "count", count),
//                (rs, rowNum) -> makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("year", year);
        map.addValue("count", count);
        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, map, new FilmRowMapper()));

        log.info("Поиск ТОП {} фильмов за {} год дал {} резльтатов", count, year, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilmsByGenre(int genreId, int count) {
        final String sql = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE, M.CATEGORY_MPA_ID, M.NAME, " +
//                "COUNT(L.USER_ID) " +
                "COUNT(L.USER_ID) + F.RATE AS RT " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE F.FILM_ID IN (" +
                "SELECT FILM_ID " +
                "FROM FILM_GENRES " +
                "WHERE GENRE_ID = :genreId) " +
                "GROUP BY F.FILM_ID " +
//                "ORDER BY COUNT(L.USER_ID) " +
                "ORDER BY RT DESC " +
                "LIMIT :count";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("genreId", genreId, "count", count),
//                (rs, rowNum) -> makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("genreId", genreId);
        map.addValue("count", count);
        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, map, new FilmRowMapper()));

        log.info("Поиск ТОП {} фильмов в жанре c ID {} дал {} резльтатов", count, genreId, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {

        final String sql = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE, " +
                "M.CATEGORY_MPA_ID, M.NAME, COUNT(L.USER_ID) + F.RATE AS RT " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY RT DESC " +
                "LIMIT :count";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("count", count),
//                (rs, rowNum) -> makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, Map.of("count", count), new FilmRowMapper()));

        log.info("Поиск ТОП {} фильмов дал {} резльтатов", count, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> findCommonFilms(int userId, int friendId) {

        final String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATE, M.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID=M.CATEGORY_MPA_ID " +
                "JOIN LIKES L ON F.film_id = L.film_id " +
                "WHERE L.user_id = :userId AND F.FILM_ID IN (" +
                "SELECT FL.FILM_ID " +
                "FROM FILMS AS FL " +
                "JOIN LIKES LI ON FL.film_id = LI.film_id " +
                "WHERE LI.user_id = :friendId" +
                ")";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("userId", userId, "friendId", friendId),
//                (rs, rowNum) -> makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, Map.of("userId", userId, "friendId", friendId), new FilmRowMapper()));

        log.info("Поиск общих фильмов пользователей {} и {} дал {} результатов", userId, friendId, filmList.size());
        return filmList;
    }

    /**
     * Метод для валидации. Если запрашивать через getFilmById(), то в нем будет исполнено 3 доп. запроса к БД:
     * на жанры, режиссеров и лайки, для нужд валидации эти запросы будут излишни.
     */
    @Override
    public boolean checkFilmById(long filmId) {
        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE AS RT, " +
                "F.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "WHERE F.FILM_ID = :filmId";
        List<Film> filmList = jdbcOperations.query(sql, Map.of("filmId", filmId), new FilmRowMapper());
        if (!filmList.isEmpty()) {
            log.info("Найден фильм с ID: {} и названием {} ", filmList.get(0).getId(), filmList.get(0).getName());
            return true;
        } else {
            log.info("Фильм c идентификатором {} не найден в БД", filmId);
            return false;
        }
    }

    @Override
    public List<Film> getRecommendations(long userId) {
        final String sql = "SELECT * " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "WHERE F.FILM_ID IN (" +
                "SELECT DISTINCT L.FILM_ID " +
                "FROM LIKES AS L " +
                "WHERE L.USER_ID IN (" +
                "SELECT FL1.USER_ID " +
                "FROM LIKES AS FL1 " +
                "JOIN LIKES AS FL2 ON FL1.FILM_ID = FL2.FILM_ID " +
                "WHERE FL2.USER_ID = :userId " +
                "AND FL1.USER_ID != :userId " +
                "GROUP BY FL1.USER_ID " +
                "ORDER BY COUNT(FL1.USER_ID) DESC " +
                "LIMIT 3" +
                ")" +
                ") " +
                "AND F.FILM_ID NOT IN (" +
                "SELECT FILM_ID " +
                "FROM LIKES " +
                "WHERE USER_ID = :userId" +
                ") " +
                "ORDER BY F.FILM_ID";

//        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMap();
//        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMap();
//        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMap();
//
//        List<Film> filmList = jdbcOperations.query(sql, Map.of("userId", userId), (rs, rowNum) ->
//                makeFilm(rs, filmsGenresMap, filmsDirectorsMap, filmLikesMap));

        List<Film> filmList = makeFilmsList(jdbcOperations.query(sql, Map.of("userId", userId), new FilmRowMapper()));

        log.info("Для пользователя с ID {} рекомендовано {} фильмов", userId, filmList.size());
        return filmList;
    }

    /**
     * 1. Выбрана структура с отдельным классом FilmRowMapper для получения простой сущности film, без списков жанров,
     * лайков, режиссеров. Для одного фильма добавления списков жанров, режиссеров и лайков происходит в методе addFilm
     * и upgradeFilm через сеттор.
     * 2. При запросе списка фильмов по фильтрам, в начале получается писок фильмов с помощью объекта FilmRowMapper, затем
     * из нее создается список с ID этих фильмов. Список ID передается в методы получения хэш таблиц жанров, режиссеров
     * и лайков по ID фильмов. Таким образом увеличичвается количество итераций списков, но уменьшается нагрузка на БД.
     * 3. Чтобы сократить количество итераций в отдельно случае для получения списка ВСЕХ фильмов создан метод makeFilm для мапинга. В этотметод передаются полученные заранее
     * коллекции связи всех фильмов - жанкров, фильмов - режиссеров, фильмов - лайкнувших пользователей.
     * В процессе обхода полученного из базы ResultSet, в создаваемую сущность film из коллекций добавляются жанры,
     * режиссеры, лайкнувшие пользователи. Таким образом избегаем N+1 запросов, при добавлении жанров, лайков, ежиссеров.
     * При этом при создании, изменении или запросе одного фильма нет необходимости итерироваться по указанным
     * множествам, потому что есть отдельный FilmRowMapper.
     */

    private Film makeFilm(ResultSet rs, Map<Long, Set<Genre>> filmsGenresMap, Map<Long, Set<Director>> filmsDirectorsMap,
                          Map<Long, Set<User>> filmLikesMap) throws SQLException {

        long filmId = rs.getLong("FILM_ID");

        Set<Genre> filmGenres = new HashSet<>();
        Set<Director> filmDirectors = new HashSet<>();
        Set<User> filmLikes = new HashSet<>();
        if (filmsGenresMap.containsKey(filmId)) {
            filmGenres = filmsGenresMap.get(filmId);
        }
        if (filmsDirectorsMap.containsKey(filmId)) {
            filmDirectors = filmsDirectorsMap.get(filmId);
        }
        if (filmLikesMap.containsKey(filmId)) {
            filmLikes = filmLikesMap.get(filmId);
        }

        return Film.builder()
                .id(filmId)
                .name(rs.getString("NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .rate(rs.getInt("RATE"))
                .mpa(new MpaCategory(rs.getInt("CATEGORY_MPA_ID"), rs.getString("MPA_CATEGORIES.NAME")))
                .genres(filmGenres)
                .directors(filmDirectors)
                .likes(filmLikes)
                .build();
    }

    private List<Film> makeFilmsList(List<Film> filmList) {
        List<Long> filmsIds = filmList.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, Set<Genre>> filmsGenresMap = getFilmsGenresMapByFilmsIds(filmsIds);
        Map<Long, Set<Director>> filmsDirectorsMap = getFilmsDirectorsMapByFilmsIds(filmsIds);
        Map<Long, Set<User>> filmLikesMap = getFilmsLikesMapByFilmsIds(filmsIds);

        filmList.forEach(film -> {
            film.setGenres(filmsGenresMap.getOrDefault(film.getId(), Collections.emptySet()));
            film.setDirectors(filmsDirectorsMap.getOrDefault(film.getId(), Collections.emptySet()));
            film.setLikes(filmLikesMap.getOrDefault(film.getId(), Collections.emptySet()));
        });
        return filmList;
    }

    private Map<Long, Set<Genre>> getFilmsGenresMap() {

        String sql = "SELECT FILM_ID, F.GENRE_ID AS GENRE_ID, G.NAME AS NAME " +
                "FROM FILM_GENRES F " +
                "LEFT JOIN GENRES G ON f.GENRE_ID = G.GENRE_ID " +
                "ORDER BY GENRE_ID";

        SqlRowSet rows = jdbcOperations.getJdbcOperations().queryForRowSet(sql);

        Map<Long, Set<Genre>> filmGenresMap = new HashMap<>();
        while (rows.next()) {
            Genre genre = new Genre(rows.getInt("GENRE_ID"), rows.getString("NAME"));

            Long filmId = rows.getLong("FILM_ID");
            if (filmGenresMap.containsKey(filmId)) {
                filmGenresMap.get(filmId).add(genre);
            } else {
                Set<Genre> genreSet = new HashSet<>();
                genreSet.add(genre);
                filmGenresMap.put(filmId, genreSet);
            }
        }
        return filmGenresMap;
    }

    private Map<Long, Set<Genre>> getFilmsGenresMapByFilmsIds(List<Long> filmsIds) {

        String sql = "SELECT F.FILM_ID, F.GENRE_ID AS GENRE_ID, G.NAME AS NAME " +
                "FROM FILM_GENRES F " +
                "LEFT JOIN GENRES G ON F.GENRE_ID = G.GENRE_ID " +
                "WHERE F.FILM_ID IN (:filmsIds) " +
                "ORDER BY GENRE_ID";

        SqlRowSet rows = jdbcOperations.queryForRowSet(sql, Map.of("filmsIds", filmsIds));

        Map<Long, Set<Genre>> filmGenresMap = new HashMap<>();
        while (rows.next()) {
            Genre genre = new Genre(rows.getInt("GENRE_ID"), rows.getString("NAME"));

            Long filmId = rows.getLong("FILM_ID");
            if (filmGenresMap.containsKey(filmId)) {
                filmGenresMap.get(filmId).add(genre);
            } else {
                Set<Genre> genreSet = new HashSet<>();
                genreSet.add(genre);
                filmGenresMap.put(filmId, genreSet);
            }
        }
        return filmGenresMap;
    }

    private Map<Long, Set<Director>> getFilmsDirectorsMap() {

        String sql = "SELECT FILM_ID, F.DIRECTOR_ID AS DIRECTOR_ID, D.NAME AS NAME " +
                "FROM FILM_DIRECTORS F " +
                "LEFT JOIN DIRECTORS D ON F.DIRECTOR_ID = D.DIRECTOR_ID " +
                "ORDER BY FILM_ID, DIRECTOR_ID";

        SqlRowSet rows = jdbcOperations.getJdbcOperations().queryForRowSet(sql);

        Map<Long, Set<Director>> filmDirectorsMap = new HashMap<>();
        while (rows.next()) {
            Director director = new Director(rows.getInt("DIRECTOR_ID"), rows.getString("NAME"));

            Long filmId = rows.getLong("FILM_ID");
            if (filmDirectorsMap.containsKey(filmId)) {
                filmDirectorsMap.get(filmId).add(director);
            } else {
                Set<Director> directorsSet = new HashSet<>();
                directorsSet.add(director);
                filmDirectorsMap.put(filmId, directorsSet);
            }
        }
        return filmDirectorsMap;
    }

    private Map<Long, Set<Director>> getFilmsDirectorsMapByFilmsIds(List<Long> filmsIds) {

        String sql = "SELECT FILM_ID, F.DIRECTOR_ID AS DIRECTOR_ID, D.NAME AS NAME " +
                "FROM FILM_DIRECTORS F " +
                "LEFT JOIN DIRECTORS D ON F.DIRECTOR_ID = D.DIRECTOR_ID " +
                "WHERE FILM_ID IN (:filmsIds) " +
                "ORDER BY FILM_ID, DIRECTOR_ID";

        SqlRowSet rows = jdbcOperations.queryForRowSet(sql, Map.of("filmsIds", filmsIds));

        Map<Long, Set<Director>> filmDirectorsMap = new HashMap<>();
        while (rows.next()) {
            Director director = new Director(rows.getInt("DIRECTOR_ID"), rows.getString("NAME"));

            Long filmId = rows.getLong("FILM_ID");
            if (filmDirectorsMap.containsKey(filmId)) {
                filmDirectorsMap.get(filmId).add(director);
            } else {
                Set<Director> directorsSet = new HashSet<>();
                directorsSet.add(director);
                filmDirectorsMap.put(filmId, directorsSet);
            }
        }
        return filmDirectorsMap;
    }

    private Map<Long, Set<User>> getFilmsLikesMap() {
        String sql = "SELECT L.FILM_ID, U.* " +
                "FROM LIKES L " +
                "JOIN USERS U ON L.USER_ID = U.USER_ID";
        // проверить, что будет, если аргумент будет равен null - вернет ли полный список, не по условию.

        SqlRowSet rows = jdbcOperations.getJdbcOperations().queryForRowSet(sql);
        Map<Long, Set<User>> filmLikesMap = new HashMap<>();
        while (rows.next()) {
            User user = new User(
                    rows.getInt("USER_ID"),
                    rows.getString("EMAIL"),
                    rows.getString("LOGIN"),
                    rows.getString("NAME"),
                    rows.getDate("BIRTHDAY") != null ? rows.getDate("BIRTHDAY").toLocalDate() : null
            );
            Long filmId = rows.getLong("FILM_ID");
            if (filmLikesMap.containsKey(filmId)) {
                filmLikesMap.get(filmId).add(user);
            } else {
                Set<User> usersSet = new HashSet<>();
                usersSet.add(user);
                filmLikesMap.put(filmId, usersSet);
            }
        }
        return filmLikesMap;
    }

    private Map<Long, Set<User>> getFilmsLikesMapByFilmsIds(List<Long> filmsIds) {
        String sql = "SELECT L.FILM_ID, U.* " +
                "FROM LIKES L " +
                "JOIN USERS U ON L.USER_ID = U.USER_ID " +
                "WHERE L.FILM_ID IN (:filmsIds) ";
        // проверить, что будет, если аргумент будет равен null - вернет ли полный список, не по условию.

        SqlRowSet rows = jdbcOperations.queryForRowSet(sql, Map.of("filmsIds", filmsIds));
        Map<Long, Set<User>> filmLikesMap = new HashMap<>();
        while (rows.next()) {
            User user = new User(
                    rows.getInt("USER_ID"),
                    rows.getString("EMAIL"),
                    rows.getString("LOGIN"),
                    rows.getString("NAME"),
                    rows.getDate("BIRTHDAY") != null ? rows.getDate("BIRTHDAY").toLocalDate() : null
            );
            Long filmId = rows.getLong("FILM_ID");
            if (filmLikesMap.containsKey(filmId)) {
                filmLikesMap.get(filmId).add(user);
            } else {
                Set<User> usersSet = new HashSet<>();
                usersSet.add(user);
                filmLikesMap.put(filmId, usersSet);
            }
        }
        return filmLikesMap;
    }

    private void addGenresForFilm(Integer genreId, Long filmId) {
        String sql = "INSERT INTO FILM_GENRES(FILM_ID, GENRE_ID) VALUES ( :filmId, :genreId )";
        jdbcOperations.update(sql, Map.of("filmId", filmId, "genreId", genreId));
        log.info("Добавлен жанр с ID: {} для фильма c ID {}", genreId, filmId);
    }

    private void deleteGenresOfFilm(long filmId) {
        String sql = "DELETE FROM FILM_GENRES WHERE FILM_ID = :filmId";
        int count = jdbcOperations.update(sql, Map.of("filmId", filmId));
        log.info("Удалено жанров {} для фильма c ID {}", count, filmId);
    }

    private void addDirectorToFilm(long filmId, int directorId) {
        String sql = "INSERT INTO FILM_DIRECTORS(FILM_ID, DIRECTOR_ID) VALUES ( :filmId, :directorId )";
        int count = jdbcOperations.update(sql, Map.of("filmId", filmId, "directorId", directorId));
        log.info("Добавлено {} режиссеров для фильма c ID {}", count, filmId);
    }

    private void deleteDirectorsOfFilm(long filmId) {
        String sql = "DELETE FROM FILM_DIRECTORS WHERE FILM_ID = :filmId";
        int count = jdbcOperations.update(sql, Map.of("filmId", filmId));
        log.info("Удалено {} режиссеров для фильма c ID {}", count, filmId);
    }

    private Set<Director> getDirectorListByFilmId(Long filmId) {
        String sql = "SELECT FD.DIRECTOR_ID, D.NAME " +
                "FROM FILM_DIRECTORS AS FD " +
                "JOIN DIRECTORS AS D ON FD.DIRECTOR_ID = D.DIRECTOR_ID " +
                "WHERE FD.FILM_ID = :filmId";
        Set<Director> directorsSet = new HashSet<>(jdbcOperations.query(sql, Map.of("filmId", filmId), new DirectorRowMapper()));
        log.info("Для фильма {} найдено {} режиссеров", filmId, directorsSet.size());
        return directorsSet;
    }

    private Set<User> getLikesByFilmId(Long filmId) {
        String sql = "SELECT U.* " +
                "FROM LIKES AS L " +
                "JOIN USERS AS U ON L.USER_ID = U.USER_ID " +
                "WHERE L.FILM_ID = :filmId";
        Set<User> usersSet = new HashSet<>(jdbcOperations.query(sql, Map.of("filmId", filmId), new UserRowMapper()));
        log.info("Для фильма {} найдено {} лайкнувших пользователей", filmId, usersSet.size());
        return usersSet;
    }
}
