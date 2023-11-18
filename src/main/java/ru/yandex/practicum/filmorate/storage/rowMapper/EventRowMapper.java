package ru.yandex.practicum.filmorate.storage.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Event(rs.getLong("EVENT_ID"),
                rs.getLong("USER_ID"),
                EventType.valueOf(rs.getString("EVENT_TYPE")),
                rs.getLong("ENTITY_ID"),
                Operation.valueOf(rs.getString("OPERATION")),
                rs.getLong("TIMESTAMP"));
    }
}