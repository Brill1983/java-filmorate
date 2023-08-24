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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public Optional<Film> getFilmById(long id) {

        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE AS RT, " +
                "F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "WHERE F.FILM_ID = :id";

        List<Film> filmList = jdbcOperations.query(sql, Map.of("id", id), new FilmRowMapper());

        if (!filmList.isEmpty()) {
            log.info("Найден фильм с ID: {} и названием {} ", filmList.get(0).getId(), filmList.get(0).getName());
            Film film = filmList.get(0);
            film.setGenres(new HashSet<>(getFilmGenres(id)));
            film.setDirectors(getDirectorListByFilmId(id));
            //TODO лайки
            return Optional.of(film);
        } else {
            log.info("Фильм c идентификатором {} не найден в БД", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getFilmsList() {
        String sql = "SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, " +
                "F.DESCRIPTION, F.DURATION, F.RATE AS RT, F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID";

//        Map<Genre, List<Long>> filmsGenresMap = getFilmsGenresMap();
        List<Film> filmList = jdbcOperations.query(sql, (rs, rowNum) -> makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
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
//        map.addValue("rate", film.getRate());
        jdbcOperations.update(sql, map, keyHolder);
        long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);
        film.getGenres()
                .forEach(genre -> addGenresForFilm(genre.getId(), filmId));
        film.getDirectors()
                .forEach(director -> addDirectorToFilm(filmId, director.getId()));
        log.info("В базу занесен фильм с идентификатором {}", filmId);
        return getFilmById(film.getId()).get();
    }

    @Override
    public boolean delete(long filmId) { //TODO попробовать delete on cascade
        // TODO deleteLikesByFilmId(filmId);
        String sqlQuery = "DELETE FROM FILM_GENRES WHERE FILM_ID = :filmId";
        jdbcOperations.update(sqlQuery, Map.of("filmId", filmId));

        sqlQuery = "DELETE  FROM LIKES WHERE FILM_ID = :filmId";
        jdbcOperations.update(sqlQuery, Map.of("filmId", filmId));

        //TODO добавить удаление ревью

        sqlQuery = "DELETE FROM FILM_DIRECTORS WHERE FILM_ID = :filmId";
        jdbcOperations.update(sqlQuery, Map.of("filmId", filmId));

        sqlQuery = "DELETE FROM FILMS WHERE FILM_ID = :filmId";
        int count = jdbcOperations.update(sqlQuery, Map.of("filmId", filmId));
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
//        map.addValue("rate", film.getRate());

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
    public List<Genre> getFilmGenres(long id) {
        String sql = "SELECT FG.GENRE_ID, G2.NAME FROM FILM_GENRES AS FG " +
                "JOIN GENRES AS G2 on FG.GENRE_ID = G2.GENRE_ID " +
                "WHERE FG.FILM_ID = :id ORDER BY G2.GENRE_ID";
        List<Genre> genres = jdbcOperations.query(sql, Map.of("id", id), new GenreRowMapper());
        log.info("Для фильма {} найдено жанров {}", id, genres.size());
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

        List<Film> filmList = jdbcOperations.query(sql, Map.of("directorId", directorId), (rs, rowNum) ->
                makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Для режиссера {} найдено {} фильмов отсортированных по году выпуска", directorId, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getDirectorFilmListByLikes(int directorId) {

        final String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, M.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS as F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "JOIN FILM_DIRECTORS FD ON F.FILM_ID = FD.FILM_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE FD.DIRECTOR_ID = :directorId " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(L.USER_ID) DESC";

        List<Film> filmList = jdbcOperations.query(sql, Map.of("directorId", directorId), (rs, rowNum) ->
                makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Для режиссера {} найдено {} фильмов отсортированных по по популярности", directorId, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> searchFilmsByDirAndName(String query) {
        String regex = "%" + query + "%";
        String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, M.CATEGORY_MPA_ID, M.NAME " +
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

        List<Film> filmList = jdbcOperations.query(sql, Map.of("regex", regex), (rs, rowNum) ->
                makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Поиск по именам режиссеров и названиям дал {} резльтатов", filmList.size());
        return filmList;
    }

    @Override
    public List<Film> searchFilmsByName(String query) {
        String regex = "%" + query + "%";
        String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, M.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID " +
                "WHERE UPPER(F.NAME) LIKE UPPER(:regex) " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(L.USER_ID) DESC";

        List<Film> filmList = jdbcOperations.query(sql, Map.of("regex", regex), (rs, rowNum) ->
                makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Поиск по названиям дал {} резльтатов", filmList.size());
        return filmList;
    }

    @Override
    public List<Film> searchFilmsByDir(String query) {
        String regex = "%" + query + "%";
        String sql = "SELECT F.FILM_ID, F.NAME, DESCRIPTION, RELEASE_DATE, DURATION, M.CATEGORY_MPA_ID, M.NAME " +
                "FROM FILMS AS F " +
                "join MPA_CATEGORIES as M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "where F.FILM_ID IN (" +
                "SELECT FD.FILM_ID " +
                "FROM FILM_DIRECTORS FD " +
                "LEFT JOIN DIRECTORS D on D.DIRECTOR_ID = FD.DIRECTOR_ID " +
                "WHERE UPPER(D.NAME) LIKE UPPER(:regex)" +
                ") " +
                "group by F.FILM_ID " +
                "order by COUNT(L.USER_ID) DESC";

        List<Film> filmList = jdbcOperations.query(sql, Map.of("regex", regex), (rs, rowNum) ->
                makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Поиск по именам режиссеров дал {} резльтатов", filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilmsByYearAndGenre(int genreId, int year, int count) {
        final String sqlQuery = "SELECT F.FILM_ID, " +
                "F.NAME, " +
                "F.RELEASE_DATE, " +
                "F.DESCRIPTION, " +
                "F.DURATION, " +
                "M.CATEGORY_MPA_ID, " +
                "M.NAME, " +
                "COUNT(L.USER_ID) " +
                "from FILMS as F " +
                "join MPA_CATEGORIES as M on F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "where F.FILM_ID IN (" +
                "select FILM_ID " +
                "from FILM_GENRES " +
                "where GENRE_ID = :genreId) " +
                "and extract(YEAR from F.RELEASE_DATE) = :year " +
                "group by F.FILM_ID " +
                "order by COUNT(L.USER_ID) " +
                "limit :count";

        List<Film> filmList = jdbcOperations.query(sqlQuery, Map.of("genreId", genreId, "year", year, "count", count),
                (rs, rowNum) -> makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Поиск ТОП {} фильмов в жанре c ID {} за {} год дал {} резльтатов", count, genreId, year, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilmsByYear(int year, int count) {
        final String sqlQuery = "SELECT F.FILM_ID, " +
                "F.NAME, " +
                "F.RELEASE_DATE, " +
                "F.DESCRIPTION, " +
                "F.DURATION, " +
                "M.CATEGORY_MPA_ID, " +
                "M.NAME, " +
                "COUNT(L.USER_ID) " +
                "from FILMS as F " +
                "join MPA_CATEGORIES as M on F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "where extract(YEAR from F.RELEASE_DATE) = :year " +
                "group by F.FILM_ID " +
                "order by COUNT(L.USER_ID) " +
                "limit :count";

        List<Film> filmList = jdbcOperations.query(sqlQuery, Map.of("year", year, "count", count),
                (rs, rowNum) -> makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Поиск ТОП {} фильмов за {} год дал {} резльтатов", count, year, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilmsByGenre(int genreId, int count) {
        final String sqlQuery = "SELECT F.FILM_ID, " +
                "F.NAME, " +
                "F.RELEASE_DATE, " +
                "F.DESCRIPTION, " +
                "F.DURATION, " +
                "M.CATEGORY_MPA_ID, " +
                "M.NAME, " +
                "COUNT(L.USER_ID) " +
                "from FILMS as F " +
                "join MPA_CATEGORIES as M on F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "where F.FILM_ID IN (" +
                "select FILM_ID " +
                "from FILM_GENRES " +
                "where GENRE_ID = :genreId) " +
                "group by F.FILM_ID " +
                "order by COUNT(L.USER_ID) " +
                "limit :count";

        List<Film> filmList = jdbcOperations.query(sqlQuery, Map.of("genreId", genreId,"count", count),
                (rs, rowNum) -> makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Поиск ТОП {} фильмов в жанре c ID {} дал {} резльтатов", count, genreId, filmList.size());
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        final String sqlQuery = "SELECT F.FILM_ID, " +
                "F.NAME, " +
                "F.RELEASE_DATE, " +
                "F.DESCRIPTION, " +
                "F.DURATION, " +
                "M.CATEGORY_MPA_ID, " +
                "M.NAME, " +
                "COUNT(L.USER_ID) " +
                "from FILMS as F " +
                "join MPA_CATEGORIES as M on F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "group by F.FILM_ID " +
                "order by COUNT(L.USER_ID) DESC " +
                "limit :count";

        List<Film> filmList = jdbcOperations.query(sqlQuery,  Map.of("count", count),
                (rs, rowNum) -> makeFilm(rs, getFilmsGenresMap(), getFilmsDirectorsMap()));
        log.info("Поиск ТОП {} фильмов дал {} резльтатов", count, filmList.size());
        return filmList;
    }

    /**
     *  Выбрана структура с отдельным классом FilmRowMapper для получения простой сущности film, без списков жанров,
     * лайков, режиссеров. Для одного фильма добавления списков жанров, режиссеров и лайков происходит в методе addFilm
     * и upgradeFilm через сеттор.
     * Отдельно создан метод makeFilm для мапинга списков фильмов, в который мепедаются один полученные заранее
     * коллекции связи всех фильмов - жанкров, фильмов - режиссеров, фильмов - лайкнувших пользователей.
     * В процессе обхода полученного из базы ResultSet в создаваемую сущность film из множеств добавляются жанры,
     * режиссеры, лайкнувшие пользователи. Таким образом избегаем N+1 запросов, при добавлении жанров, лайков, ежиссеров.
     * При этом при создании, изменении или запросе одного фильма нет необходимости итерироваться по указанным
     * множествам, потому что есть отдельный FilmRowMapper.
     */

    private Film makeFilm(ResultSet rs, Map<Long, Set<Genre>> filmsGenresMap, Map<Long, Set<Director>> filmsDirectorsMap) throws SQLException {

        long filmId = rs.getLong("FILM_ID");

//        Set<Genre> filmGenres = new HashSet<>();
//        for (Genre genre : filmsGenresMap.keySet()) { // итерация по мапе, где ключ: жанр, значение: список с ID фильмов.
//            if (filmsGenresMap.get(genre).contains(filmId)) { // если ID фильма содержится в списке-значении мапы - то формируем список жанров для фильма
//                filmGenres.add(genre);
//            }
//        }
        Set<Genre> filmGenres = new HashSet<>();
        Set<Director> filmDirectors = new HashSet<>();
        if(filmsGenresMap.containsKey(filmId)) {
            filmGenres = filmsGenresMap.get(filmId);
        }
        if(filmsDirectorsMap.containsKey(filmId)) {
            filmDirectors = filmsDirectorsMap.get(filmId);
        }
        // TODO добавить сборку лайков

        return Film.builder() // TODO поменять на конструктор
                .id(filmId)
                .name(rs.getString("FILM_NAME"))
                .description(rs.getString("DESCRIPTION"))
//                .rate(rs.getInt("RT"))
                .duration(rs.getInt("DURATION"))
                .mpa(new MpaCategory(rs.getInt("CATEGORY_MPA_ID"), rs.getString("MPA_NAME")))
                .genres(filmGenres)
                .directors(filmDirectors)
                .build();
    }

    private Map<Long, Set<Genre>> getFilmsGenresMap() {

        String sql = "SELECT FILM_ID, F.GENRE_ID AS GENRE_ID, G.NAME AS NAME " +
                "FROM FILM_GENRES F " +
                "LEFT JOIN GENRES G ON f.GENRE_ID = G.GENRE_ID " +
                "ORDER BY GENRE_ID";

        SqlRowSet rows = jdbcOperations.getJdbcOperations().queryForRowSet(sql);

        Map<Long, Set<Genre>> filmGenresMap = new HashMap<>();
        while(rows.next()) {
            Genre genre = new Genre(rows.getInt("GENRE_ID"), rows.getString("NAME"));

            Long filmId = rows.getLong("FILM_ID"); //added
            if(filmGenresMap.containsKey(filmId)) {
                filmGenresMap.get(filmId).add(genre);
            } else {
                Set<Genre> genreSet = new HashSet<>();
                genreSet.add(genre);
                filmGenresMap.put(filmId, genreSet);
            }

//            if(filmGenreMap.containsKey(genre)) {
//                filmGenreMap.get(genre).add(rows.getLong("FILM_ID"));
//            } else {
//                List<Long> filmsIdsList = new ArrayList<>();
//                filmsIdsList.add(rows.getLong("FILM_ID"));
//                filmGenreMap.put(genre, filmsIdsList);
//            }
        }
        return filmGenresMap;
    }

    private void addGenresForFilm(Integer genreId, Long filmId) {
        String sql = "INSERT INTO FILM_GENRES(FILM_ID, GENRE_ID) VALUES ( :filmId, :genreId )";
        jdbcOperations.update(sql, Map.of("filmId", filmId, "genreId", genreId));
        log.info("Добавлен жанр с ID: {} для фильма c ID {}", genreId, filmId);
    }

    private void deleteGenresOfFilm(long filmId) {
        String sql = "DELETE FROM FILM_GENRES WHERE FILM_ID = :filmId";
        MapSqlParameterSource map = new MapSqlParameterSource();
        int count = jdbcOperations.update(sql, Map.of("filmId", filmId)); // TODO проверить возврат count
        log.info("Удалено жанров {} для фильма c ID {}", count, filmId);
    }

    private void addDirectorToFilm(long filmId, int directorId) {
        String sql = "INSERT INTO FILM_DIRECTORS(FILM_ID, DIRECTOR_ID) VALUES ( :filmId, :directorId )";
        int count = jdbcOperations.update(sql, Map.of("filmId", filmId, "directorId", directorId));
        log.info("Добавлено {} режиссеров для фильма c ID {}", count, filmId);
    }

    private void deleteDirectorsOfFilm(long filmId) {
        String deleteSqlQuery = "DELETE FROM FILM_DIRECTORS WHERE FILM_ID = :filmId";
        int count = jdbcOperations.update(deleteSqlQuery, Map.of("filmId", filmId));
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

    private Map<Long, Set<Director>> getFilmsDirectorsMap() {

        String sql = "SELECT FILM_ID, F.DIRECTOR_ID AS DIRECTOR_ID, D.NAME AS NAME " +
                "FROM FILM_DIRECTORS F " +
                "LEFT JOIN DIRECTORS D ON F.DIRECTOR_ID = D.DIRECTOR_ID " +
                "ORDER BY DIRECTOR_ID";

        SqlRowSet rows = jdbcOperations.getJdbcOperations().queryForRowSet(sql);

        Map<Long, Set<Director>> filmDirectorsMap = new HashMap<>();
        while (rows.next()) {
            Director director = new Director(rows.getInt("DIRECTOR_ID"), rows.getString("NAME"));

            Long filmId = rows.getLong("FILM_ID"); //added
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
}
