package ru.yandex.practicum.filmorate.storage.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaCategory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder() // TODO change for constructor
                .id(rs.getLong("FILM_ID"))
                .name(rs.getString("FILM_NAME"))
                .description(rs.getString("DESCRIPTION"))
//                .rate(rs.getInt("RT"))
                .duration(rs.getInt("DURATION"))
                .mpa(new MpaCategory(rs.getInt("CATEGORY_MPA_ID"), rs.getString("MPA_NAME")))
                .build();
    }
}
